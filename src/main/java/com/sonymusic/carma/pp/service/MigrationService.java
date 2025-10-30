package com.sonymusic.carma.pp.service;

import com.sonymusic.carma.pp.model.MigrationStatistics;
import com.sonymusic.carma.pp.persistence.converter.ClearanceValue;
import com.sonymusic.carma.pp.persistence.entity.*;
import com.sonymusic.carma.pp.persistence.repository.*;
import com.sonymusic.carma.sapi.preference.PreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

	public static final Integer MOD_USER = 99999998;
	private final InputRightsViewRepository rightsRepository;
	private final DigitalRightsEuRepository digitalRightsEURepository;
	private final DigitalRightsUsRepository digitalRightsUSRepository;
	private final InputRightsEURepository inputRightsEURepository;
	private final InputRightsUSRepository inputRightsUSRepository;
	private final ProtocolRepository protocolRepository;
	private final MailService mailService;
	private final PreferenceRepository preferenceRepository;
	private final DraValidator draValidator;

	private static final Integer PERPETUAL_HIERARCHY_ID = 21;
	private static final Integer ADMIN_USER = 99999998;

	public void migrateByContract(List<Integer> contractIds) {
		log.info("Migration started with contracts {}", contractIds);
		MigrationStatistics statistics = migrateAndValidate(rightsRepository.findByContractIdInAndStatusFlagIsNull(contractIds));
		statistics.addContractIds(contractIds);
		mailService.sendStatusMail(statistics);
		log.info("Migration completed with contracts {}", contractIds);
	}

	public void migrateByCountry(List<String> countryCodes) {
		log.info("Migration started with countries {}", countryCodes);
		MigrationStatistics statistics = migrateAndValidate(rightsRepository.findByCountryIdInAndStatusFlagIsNull(countryCodes));
		statistics.addCountryCodes(countryCodes);
		mailService.sendStatusMail(statistics);
		log.info("Migration completed with countries {}", countryCodes);
	}

	private MigrationStatistics migrateAndValidate(List<InputPerpetualRightsViewEntity> rightsInput) {
		Integer actionId = protocolRepository.getNextActionId();
		MigrationStatistics statistics = new MigrationStatistics(actionId);
		//filter and manage invalid rows, exists only for other environment than Production
		//missing contract
		List<InputPerpetualRightsViewEntity> invalidRightsMissingContract = rightsInput.stream().filter(item -> item.getContractId() == null).toList();
		createSkippedProtocolEntry(invalidRightsMissingContract, actionId, null, ResultStatus.FAILED, "Missing Contract");
		statistics.incrementFailedCount(invalidRightsMissingContract.size());
		//missing Recording
		List<InputPerpetualRightsViewEntity> validRights = rightsInput.stream().filter(item -> item.getContractId() != null).toList();
		List<InputPerpetualRightsViewEntity> invalidRightsOnContractLevel = validRights.stream().filter(item -> item.getContractDRNodeInstanceId() == null).toList();
		createSkippedProtocolEntry(invalidRightsOnContractLevel, actionId, null, ResultStatus.FAILED, "Missing Recording");
		statistics.incrementFailedCount(invalidRightsOnContractLevel.size());
		//Run migration for valid rows
		List<InputPerpetualRightsViewEntity> validRightsWithRecording = rightsInput.stream().filter(item -> item.getContractDRNodeInstanceId() != null).toList();
		Map<Integer, List<InputPerpetualRightsViewEntity>> groupedByRecording = validRightsWithRecording.stream()
			.collect(Collectors.groupingBy(InputPerpetualRightsViewEntity::getRecordingDRNodeInstanceId));
		log.info("Migrating {} Recording levels", groupedByRecording.size());
		Set<Integer> instanceIdToBeMigrated = new HashSet<>();
		groupedByRecording.forEach((key, value) -> statistics.merge(migrate(key, value, actionId, instanceIdToBeMigrated)));

		// execute validation for those where No Digital Rights was defined before
		Set<InputPerpetualRightsViewEntity> noDigitalRighstOnAnyLevelInputRows =
			validRightsWithRecording.stream().filter(item -> StringUtils.isEmpty(item.getContractMasterClearance())
				&& StringUtils.isEmpty(item.getPeriodMasterClearance())
				&& StringUtils.isEmpty((item.getRecordingMasterClearance()))).collect(Collectors.toSet());

		Set<Integer> contractIdsToBeValidate = protocolRepository.getAllSuccessedAndNewDigitalRights(
			noDigitalRighstOnAnyLevelInputRows.stream().map(InputPerpetualRightsViewEntity::getId).collect(Collectors.toSet()));
		List<Integer> failedValidation = validateDigitalRights(contractIdsToBeValidate);
		statistics.addFailedValidationContractIds(failedValidation);
		return statistics;
	}

	private void createSkippedProtocolEntry(List<InputPerpetualRightsViewEntity> rights, Integer actionId, Integer nodeInstanceId, ResultStatus resultStatus, String text) {
		for (InputPerpetualRightsViewEntity item : rights) {
			log.info("{} for contract={} and recording={}", text, item.getContractNumber(), item.getRecording());
			createAndSaveProtocol(actionId, item, nodeInstanceId, null, null, resultStatus, text);
			if (item.isUs()) {
				inputRightsUSRepository.updateProcessed(item.getId());
			} else {
				inputRightsEURepository.updateProcessed(item.getId());
			}
		}
	}

	private MigrationStatistics migrate(Integer digitalRightsInstanceId, List<InputPerpetualRightsViewEntity> rights, Integer actionId, Set<Integer> instanceIdToBeMigrated) {
		MigrationStatistics statistics = new MigrationStatistics();
		try {
			//filter and manage the users' failures
			Set<String> prExceptionsForOneNodeInstance = rights.stream().map(InputPerpetualRightsViewEntity::getPerpetualRightsException).collect(Collectors.toSet());
			if (prExceptionsForOneNodeInstance.size() > 1) {
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, "Inconsistency of different Exception data");
				statistics.incrementFailedCount(rights.size());
				return statistics;
			}
			InputPerpetualRightsViewEntity rightOne = rights.get(0);
			if (StringUtils.isNotEmpty(rightOne.getPerpetualRightsException()) && ClearanceValue.INVALID.equals(
				convertToEntityAttributeFromInput(rightOne.getPerpetualRightsException()))) {
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, "Invalid Exception value");
				statistics.incrementFailedCount(rights.size());
				return statistics;
			}
			//define the right data for the future Digital Right
			String inheritedClearanceValue = null;
			Integer toBeInsertedNodeInstanceId;
			String territory = rightOne.getTerritory();
			String territoryToBeInserted = null;
			if (StringUtils.isNotEmpty(rightOne.getRecordingMasterClearance())) {
				inheritedClearanceValue = rightOne.getRecordingMasterClearance();
				toBeInsertedNodeInstanceId = rightOne.getRecordingDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getRecordingDRTerritory()) && !Objects.equals(territory, rightOne.getRecordingDRTerritory())) {
					territoryToBeInserted = rightOne.getRecordingDRTerritoryExpression();
				}
			} else if (StringUtils.isNotEmpty(rightOne.getPeriodMasterClearance())) {
				inheritedClearanceValue = rightOne.getPeriodMasterClearance();
				toBeInsertedNodeInstanceId = rightOne.getPeriodDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getPeriodDRTerritory()) && !Objects.equals(territory, rightOne.getPeriodDRTerritory())) {
					territoryToBeInserted = rightOne.getPeriodDRTerritoryExpression();
				}
			} else if (StringUtils.isNotEmpty(rightOne.getContractMasterClearance())) {
				inheritedClearanceValue = rightOne.getContractMasterClearance();
				toBeInsertedNodeInstanceId = rightOne.getContractDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getContractDRTerritory()) && !Objects.equals(territory, rightOne.getContractDRTerritory())) {
					territoryToBeInserted = rightOne.getContractDRTerritoryExpression();
				}
			} else {
				toBeInsertedNodeInstanceId = rightOne.getContractDRNodeInstanceId();
			}

			String intendedClearanceValue = StringUtils.isNotEmpty(rightOne.getPerpetualRightsException()) ?
				rightOne.getPerpetualRightsException() :
				rightOne.getPerpetualRights();
			ClearanceValue intendedClearance = convertToEntityAttributeFromInput(intendedClearanceValue);
			if (Objects.equals(convertToEntityAttributeFromInput(inheritedClearanceValue), intendedClearance)) {
				//If the Intended Clearance Value is equals the Inherited Clearance Value, then no backfill clearance is necessary.
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, "Intended Clearance = Inherited Clearance");
				statistics.incrementSkippedCount(rights.size());
				return statistics;
			}

			//Skipp the already migrated rows
			if (instanceIdToBeMigrated.contains(toBeInsertedNodeInstanceId)) {
				//when we've already created for contract level, this will be run for all recordings, if it has no different node_instance to be migrated
				String text = String.format("Calculated nodeInstance has already been migrated for nodeInstanceId=%s ", toBeInsertedNodeInstanceId);
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, text);
				statistics.incrementSkippedCount(rights.size());
				return statistics;
			}

			//create new Digital Right
			Integer digitalRightsEu = null;
			Integer digitalRightsUs = null;
			//If the Intended Clearance Value is different from the Inherited Clearance Value, then a clearance should be inserted at the same level as the Inherited Clearance Value
			// (or the Contract level if no Inherited Clearance Value exists) with the same territories scope as the Inherited Clearance Value,
			if (rightOne.isUs()) {
				DigitalRightsEntityUS saved = digitalRightsUSRepository.save(DigitalRightsEntityUS.builder()
					.nodeInstanceId(toBeInsertedNodeInstanceId)
					.cleared(intendedClearance == ClearanceValue.YES)
					.draRightsHierarchyId(PERPETUAL_HIERARCHY_ID)
					.territory(territoryToBeInserted)
					.modStamp(LocalDateTime.now())
					.modUser(ADMIN_USER)
					.statusFlag(true)
					.build());
				digitalRightsUs = saved.getId();
				createAndSaveProtocol(actionId, rightOne, toBeInsertedNodeInstanceId, digitalRightsEu, digitalRightsUs, ResultStatus.SUCCESS, null);
				inputRightsUSRepository.updateProcessed(rightOne.getId());
			} else {
				DigitalRightsEntityEU saved = digitalRightsEURepository.save(DigitalRightsEntityEU.builder()
					.nodeInstanceId(toBeInsertedNodeInstanceId)
					.approvalTerm(intendedClearance == ClearanceValue.YES)
					.draRightsHierarchyId(PERPETUAL_HIERARCHY_ID)
					.territory(territoryToBeInserted)
					.modStamp(LocalDateTime.now())
					.modUser(ADMIN_USER)
					.statusFlag(true)
					.build());
				digitalRightsEu = saved.getId();
				createAndSaveProtocol(actionId, rightOne, toBeInsertedNodeInstanceId, digitalRightsEu, digitalRightsUs, ResultStatus.SUCCESS, null);
				inputRightsEURepository.updateProcessed(rightOne.getId());
			}
			//skip the other rights for the same recording
			instanceIdToBeMigrated.add(toBeInsertedNodeInstanceId);
			rights.remove(rightOne);
			String text = String.format(
				"Calculated nodeInstance has already been migrated for nodeInstanceId=%s, because of multiple linked Project/Product Family", toBeInsertedNodeInstanceId);
			createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, text);

			statistics.addRelatedContractId(rightOne.getContractId());
			statistics.incrementDigitalRightsCount(1);
			statistics.incrementSkippedCount(rights.size());
			log.info("Migrating Perpetual Right for contract {} on the node_instance {} ", rightOne.getContractNumber(), toBeInsertedNodeInstanceId.toString());

		} catch (Exception e) {
			statistics.incrementFailedCount(rights.size());
			rights.forEach(item -> createAndSaveProtocol(actionId, item, digitalRightsInstanceId, null, null, ResultStatus.FAILED, e.getMessage()));
		}
		return statistics;
	}

	private ClearanceValue convertToEntityAttributeFromInput(String value) {
		return StringUtils.isEmpty(value) ?
			null :
			("Y".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value)) ? ClearanceValue.YES
				: ("N".equalsIgnoreCase(value) || "NO".equalsIgnoreCase(value) ? ClearanceValue.NO : ClearanceValue.INVALID);
	}

	private void createAndSaveProtocol(Integer actionId, InputPerpetualRightsViewEntity right, Integer nodeInstanceId,
		Integer digitalRightsContractEuId, Integer digitalRightsContractUsId, ResultStatus resultStatus, String result) {
		ProtocolEntity protocolEntity = ProtocolEntity.builder()
			.actionId(actionId)
			.documentId(right.getDocumentId())
			.contractId(right.getContractId())
			.contractNumber(right.getContractNumber())
			.recording(right.getRecording())
			.draPerpetualRightsMigrationInputId(right.getId())
			.inputLevelNodeInstanceId(nodeInstanceId)
			.digitalRightsContractEuId(digitalRightsContractEuId)
			.digitalRightsContractUsId(digitalRightsContractUsId)
			.modUser(MOD_USER)
			.modStamp(LocalDateTime.now())
			.resultStatus(resultStatus)
			.result(result)
			.statusFlag(true)
			.build();
		protocolRepository.save(protocolEntity);
	}

	public void clearByContract(List<Integer> contractIds) {
		log.info("Deletion started with contracts {}", contractIds);
		deleteData(rightsRepository.findByContractIdInAndStatusFlagIsA(contractIds));
		log.info("Deletion completed with contracts {}", contractIds);
	}

	public void clearByCountry(List<String> countryCodes) {
		log.info("Deletion started with countries {}", countryCodes);
		deleteData(rightsRepository.findByCountryIdInAndStatusFlagIsA(countryCodes));
		log.info("Deletion completed with countries {}", countryCodes);
	}

	private void deleteData(List<InputPerpetualRightsViewEntity> rights) {
		Set<Integer> contractIds = rights.stream().map(InputPerpetualRightsViewEntity::getContractId).collect(Collectors.toSet());
		Set<Integer> inputIds = rights.stream().map(InputPerpetualRightsViewEntity::getId).collect(Collectors.toSet());
		if (!rights.isEmpty()) {
			if (rights.get(0).isUs()) {
				List<Integer> digitalRightsIds = protocolRepository.selectDigitalRightsContractUSIdByInputViewIds(inputIds);
				digitalRightsIds.forEach(digitalRightsUSRepository::deleteById);
				inputIds.forEach(inputRightsUSRepository::updateCleared);
			} else {
				List<Integer> digitalRightsIds = protocolRepository.selectDigitalRightsContractEUIdByInputViewIds(inputIds);
				digitalRightsIds.forEach(digitalRightsEURepository::deleteById);
				inputIds.forEach(inputRightsEURepository::updateCleared);
			}
			// delete validation for those where No Digital Rights was defined before
			Set<InputPerpetualRightsViewEntity> noDigitalRighstOnAnyLevelInputRows =
				rights.stream().filter(item -> StringUtils.isEmpty(item.getContractMasterClearance())
					&& StringUtils.isEmpty(item.getPeriodMasterClearance())
					&& StringUtils.isEmpty((item.getRecordingMasterClearance()))).collect(Collectors.toSet());

			Set<Integer> contractIdsToBeValidate = protocolRepository.getAllSuccessedAndNewDigitalRights(
				noDigitalRighstOnAnyLevelInputRows.stream().map(InputPerpetualRightsViewEntity::getId).collect(Collectors.toSet()));
			List<Integer> failedValidation = validateDigitalRights(contractIdsToBeValidate);

			contractIds.forEach(protocolRepository::deleteAllByContractId);

		}
	}

	private List<Integer> validateDigitalRights(Set<Integer> contractIds) {
		String url = preferenceRepository.getValueByName("carma.server.url") + "/dra-interface/digital-rights";
		List<Integer> validationFailed = new ArrayList<>();
		for (Integer contractId : contractIds) {
			if (!draValidator.validateDigitalRights(url, contractId, MOD_USER)) {
				validationFailed.add(contractId);
			}
		}
		return validationFailed;
	}

}
