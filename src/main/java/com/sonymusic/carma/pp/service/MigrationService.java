package com.sonymusic.carma.pp.service;

import com.sonymusic.carma.pp.model.MigrationStatistics;
import com.sonymusic.carma.pp.persistence.converter.ClearanceValueConverter;
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
	public static final String INCONSISTENCY_OF_DIFFERENT_EXCEPTION_DATA = "Inconsistency of different Exception data";
	public static final String MISSING_CONTRACT = "Missing Contract";
	public static final String MISSING_RECORDING = "Missing Recording";
	public static final String INVALID_EXCEPTION_VALUE = "Invalid Exception value";
	public static final String INTENDED_CLEARANCE_INHERITED_CLEARANCE = "Intended Clearance = Inherited Clearance";
	public static final String NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED = "Calculated nodeInstance has already been migrated for nodeInstanceId=%s ";
	public static final String NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED_WITH_DIFFERENT_CLEARANCE = "Calculated nodeInstance has already been migrated with different Clearance for nodeInstanceId=%s, clearance=%s";
	public static final String THERE_IS_ALREADY_A_DEFINED_DIGITAL_RIGHT = "There is already a defined digital right with the same clearance value for nodeInstanceId=%s";
	public static final String THERE_IS_ALREADY_A_DEFINED_DIGITAL_RIGHT_WITH_OTHER_ATTRIBUTES = "There is already a defined digital right with other attributes for nodeInstanceId=%s, clearance=%s, hierarchyId=%s";
	public static final String NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED_MULTIPLE = "Calculated nodeInstance has already been migrated for nodeInstanceId=%s, because of multiple linked Project/Product Family";
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
	private final CarmaRepository carmaRepository;

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
		createSkippedProtocolEntry(invalidRightsMissingContract, actionId, null, ResultStatus.FAILED, MISSING_CONTRACT);
		statistics.incrementFailedCount(invalidRightsMissingContract.size());
		//missing Recording
		List<InputPerpetualRightsViewEntity> validRights = rightsInput.stream().filter(item -> item.getContractId() != null).toList();
		List<InputPerpetualRightsViewEntity> invalidRightsOnContractLevel = validRights.stream().filter(item -> item.getContractDRNodeInstanceId() == null).toList();
		createSkippedProtocolEntry(invalidRightsOnContractLevel, actionId, null, ResultStatus.FAILED, MISSING_RECORDING);
		statistics.incrementFailedCount(invalidRightsOnContractLevel.size());

		List<InputPerpetualRightsViewEntity> validRightsWithRecording = rightsInput.stream().filter(item -> item.getContractDRNodeInstanceId() != null).toList();
		statistics = runMigrationForValidRows(statistics, validRightsWithRecording, actionId);

		return statistics;
	}

	private MigrationStatistics runMigrationForValidRows(MigrationStatistics statistics,
		List<InputPerpetualRightsViewEntity> validRightsWithRecording, Integer actionId) {
		if (!validRightsWithRecording.isEmpty()) {
			List<DigitalRightsNodeInstanceAndClearanceData> existingPerpetualRights;
			Set<Integer> contractIds = validRightsWithRecording.stream().map(InputPerpetualRightsViewEntity::getContractId).collect(Collectors.toSet());
			if (validRightsWithRecording.get(0).isUs()) {
				existingPerpetualRights = carmaRepository.getExistingPerpetualDigitalRightsForUSContracts(contractIds);
			} else {
				existingPerpetualRights = carmaRepository.getExistingPerpetualDigitalRightsForEUContracts(contractIds);
			}

			Map<Integer, List<InputPerpetualRightsViewEntity>> groupedByContract = validRightsWithRecording.stream()
				.collect(Collectors.groupingBy(InputPerpetualRightsViewEntity::getContractId));
			//Execute migration grouped by contract

			for (Integer contractId : groupedByContract.keySet()) {
				List<InputPerpetualRightsViewEntity> validRightsByContract = groupedByContract.get(contractId);
				log.info("Migrating for Contract: {} contractId = {}", validRightsByContract.get(0).getContractNumber(), contractId);
				Map<Integer, List<InputPerpetualRightsViewEntity>> groupedByRecording = validRightsByContract.stream()
					.collect(Collectors.groupingBy(InputPerpetualRightsViewEntity::getRecordingDRNodeInstanceId));
				//If of all the intended clearance values of a contract, one value occurs more often than the other, this should be considered the majority Intended Clearance Value.

				int yes = (int) groupedByRecording.values().stream()
					.map(list -> ClearanceValueConverter.convertToEntityAttributeFromInput(list.get(0).getIntendedClearance()))
					.filter(ClearanceValue.YES::equals)
					.count();

				int no = (int) groupedByRecording.values().stream()
					.map(list -> ClearanceValueConverter.convertToEntityAttributeFromInput(list.get(0).getIntendedClearance()))
					.filter(ClearanceValue.NO::equals)
					.count();

				ClearanceValue majorityClearance = (yes >= no) ? ClearanceValue.YES : ClearanceValue.NO;
				log.info("Majority Clearance = {} for Contract: {}", majorityClearance, contractId);
				log.info("Migrating {} Recording levels", groupedByRecording.size());
				Set<DigitalRightsToBeInserted> instanceIdToBeMigrated = new HashSet<>();
				groupedByRecording.forEach(
					(key, value) -> statistics.merge(migrateForOneRecording(key, value, majorityClearance, actionId, instanceIdToBeMigrated, existingPerpetualRights)));
			}
			// execute validation for those where No Digital Rights was defined before
			Set<InputPerpetualRightsViewEntity> noDigitalRighstOnAnyLevelInputRows =
				validRightsWithRecording.stream().filter(item -> StringUtils.isEmpty(item.getContractMasterClearance())
					&& StringUtils.isEmpty(item.getPeriodMasterClearance())
					&& StringUtils.isEmpty((item.getRecordingMasterClearance()))).collect(Collectors.toSet());

			Set<Integer> contractIdsToBeValidate = protocolRepository.getAllSuccessedAndNewDigitalRights(
				noDigitalRighstOnAnyLevelInputRows.stream().map(InputPerpetualRightsViewEntity::getId).collect(Collectors.toSet()));
			log.info("Execute validation for those where No Digital Rights was defined before. Count: {}", contractIdsToBeValidate.size());
			List<Integer> failedValidation = validateDigitalRights(contractIdsToBeValidate);
			statistics.addFailedValidationContractIds(failedValidation);
		}
		return statistics;
	}

	private MigrationStatistics migrateForOneRecording(Integer digitalRightsInstanceId, List<InputPerpetualRightsViewEntity> rights, ClearanceValue majorityClearance,
		Integer actionId,
		Set<DigitalRightsToBeInserted> instanceIdToBeMigrated, List<DigitalRightsNodeInstanceAndClearanceData> existingPerpetualRights) {
		MigrationStatistics statistics = new MigrationStatistics();
		try {
			//filter and manage the users' failures
			Set<String> prExceptionsForOneNodeInstance = rights.stream().map(InputPerpetualRightsViewEntity::getPerpetualRightsException).collect(Collectors.toSet());
			if (prExceptionsForOneNodeInstance.size() > 1) {
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, INCONSISTENCY_OF_DIFFERENT_EXCEPTION_DATA);
				statistics.incrementFailedCount(rights.size());
				return statistics;
			}
			InputPerpetualRightsViewEntity rightOne = rights.get(0);
			if (StringUtils.isNotEmpty(rightOne.getPerpetualRightsException()) && ClearanceValue.INVALID.equals(
				ClearanceValueConverter.convertToEntityAttributeFromInput(rightOne.getPerpetualRightsException()))) {
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, INVALID_EXCEPTION_VALUE);
				statistics.incrementFailedCount(rights.size());
				return statistics;
			}

			//If something to be inserted
			InheritedAndIntendedData dataDefinedByRules = defineTheFutureDigitalRightDataByRules(rightOne, majorityClearance);
			if (dataDefinedByRules.isSkipped()) {
				//If the Intended Clearance Value is equals the Inherited Clearance Value, then no backfill clearance is necessary.
				createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, INTENDED_CLEARANCE_INHERITED_CLEARANCE);
				statistics.incrementSkippedCount(rights.size());
				return statistics;
			}

			DigitalRightsToBeInserted toBeInserted = dataDefinedByRules.getIntendedData();

			//Skip the already migrated rows
			DigitalRightsToBeInserted existingMigrated = migratedPerpetualRightsContainsNodeInstance(instanceIdToBeMigrated, toBeInserted);
			if (existingMigrated != null) {
				if (existingMigrated.getClearanceValue() == toBeInserted.getClearanceValue()) {
					String text = String.format(NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED, toBeInserted.getNodeInstanceId());
					createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, text);
					statistics.incrementSkippedCount(rights.size());
				} else {
					String text = String.format(NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED_WITH_DIFFERENT_CLEARANCE,
						existingMigrated.getNodeInstanceId(), existingMigrated.getClearanceValue());
					createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, text);
					statistics.incrementFailedCount(rights.size());
				}
				return statistics;
			}
			//skip, if there is something manually set on the same nodeInstance
			DigitalRightsNodeInstanceAndClearanceData existingManually = existingPerpetualRightsContainsNodeInstance(existingPerpetualRights, toBeInserted);
			if (existingManually != null) {
				if (Objects.equals(ClearanceValueConverter.convertToEntityAttributeFromInput(existingManually.getClearance()), toBeInserted.getClearanceValue())
					&& existingManually.getDraRightsHierarchyId() == 21) {
					String text = String.format(THERE_IS_ALREADY_A_DEFINED_DIGITAL_RIGHT,
						existingManually.getNodeInstanceId());
					createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, text);
					statistics.incrementSkippedCount(rights.size());
				} else {
					String text = String.format(THERE_IS_ALREADY_A_DEFINED_DIGITAL_RIGHT_WITH_OTHER_ATTRIBUTES,
						existingManually.getNodeInstanceId(), existingManually.getClearance(), existingManually.getDraRightsHierarchyId());
					createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.FAILED, text);
					statistics.incrementFailedCount(rights.size());
				}
				return statistics;
			}
			//at this point remains only the data, which is to be inserted
			createNewDigitalRightsFromData(digitalRightsInstanceId, rights, actionId, instanceIdToBeMigrated, rightOne, toBeInserted, statistics);

		} catch (Exception e) {
			statistics.incrementFailedCount(rights.size());
			rights.forEach(item -> createAndSaveProtocol(actionId, item, digitalRightsInstanceId, null, null, ResultStatus.FAILED, e.getMessage()));
		}
		return statistics;
	}

	private InheritedAndIntendedData defineTheFutureDigitalRightDataByRules(InputPerpetualRightsViewEntity rightOne, ClearanceValue majorityClearance) {
		//The kernel of the CAS-15082
		//the row bundle for the same recording should be the same Clearance Value at this point, so we use only the first row to define
		ClearanceValue inheritedClearance = ClearanceValueConverter.convertToEntityAttributeFromInput(rightOne.getInheritedClearance());
		ClearanceValue intendedClearance = ClearanceValueConverter.convertToEntityAttributeFromInput(rightOne.getIntendedClearance());

		Integer toBeInsertedNodeInstanceId;
		String territory = rightOne.getTerritory();
		String toBeInsertedTerritory = null;

		if (StringUtils.isNotEmpty(rightOne.getRecordingMasterClearance())
			|| StringUtils.isNotEmpty(rightOne.getPeriodMasterClearance())) {
			//if the Intended Clearance Value equals the Recording's or Period's Clearance Value, then no backfill is necessary.
			if (inheritedClearance == intendedClearance) {
				return InheritedAndIntendedData.builder()
					.isSkipped(true)
					.build();
			}
		}

		if (StringUtils.isNotEmpty(rightOne.getRecordingMasterClearance())) {
			toBeInsertedNodeInstanceId = rightOne.getRecordingDRNodeInstanceId();
			if (StringUtils.isNotEmpty(rightOne.getRecordingDRTerritory()) && !Objects.equals(territory, rightOne.getRecordingDRTerritory())) {
				toBeInsertedTerritory = rightOne.getRecordingDRTerritoryExpression();
			}
		} else if (StringUtils.isNotEmpty(rightOne.getPeriodMasterClearance())) {
			toBeInsertedNodeInstanceId = rightOne.getPeriodDRNodeInstanceId();
			if (StringUtils.isNotEmpty(rightOne.getPeriodDRTerritory()) && !Objects.equals(territory, rightOne.getPeriodDRTerritory())) {
				toBeInsertedTerritory = rightOne.getPeriodDRTerritoryExpression();
			}
		} else if (StringUtils.isNotEmpty(rightOne.getContractMasterClearance())
			&& inheritedClearance == majorityClearance) {
			if (intendedClearance == majorityClearance) {
				return InheritedAndIntendedData.builder()
					.isSkipped(true)
					.build();
			} else {
				toBeInsertedNodeInstanceId = rightOne.getRecordingDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getRecordingDRTerritory()) && !Objects.equals(territory, rightOne.getRecordingDRTerritory())) {
					toBeInsertedTerritory = rightOne.getRecordingDRTerritoryExpression();
				}
			}
		} else { // hasMaster Clearance and inheritedClearance != majorityClearance
			// OR has no master clearance
			if (intendedClearance == majorityClearance) {
				toBeInsertedNodeInstanceId = rightOne.getContractDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getContractDRTerritory()) && !Objects.equals(territory, rightOne.getContractDRTerritory())) {
					toBeInsertedTerritory = rightOne.getContractDRTerritoryExpression();
				}
			} else {
				toBeInsertedNodeInstanceId = rightOne.getRecordingDRNodeInstanceId();
				if (StringUtils.isNotEmpty(rightOne.getRecordingDRTerritory()) && !Objects.equals(territory, rightOne.getRecordingDRTerritory())) {
					toBeInsertedTerritory = rightOne.getRecordingDRTerritoryExpression();
				}
			}
		}

		DigitalRightsToBeInserted toBeInserted = DigitalRightsToBeInserted.builder()
			.nodeInstanceId(toBeInsertedNodeInstanceId)
			.clearanceValue((intendedClearance))
			.territory(toBeInsertedTerritory).build();

		return InheritedAndIntendedData.builder()
			.inheritedClearance(inheritedClearance)
			.intendedData(toBeInserted)
			.build();
	}

	private void createNewDigitalRightsFromData(Integer digitalRightsInstanceId, List<InputPerpetualRightsViewEntity> rights, Integer actionId,
		Set<DigitalRightsToBeInserted> instanceIdToBeMigrated,
		InputPerpetualRightsViewEntity rightOne, DigitalRightsToBeInserted toBeInserted, MigrationStatistics statistics) {
		//create new Digital Right
		Integer digitalRightsEu = null;
		Integer digitalRightsUs = null;
		//If the Intended Clearance Value is different from the Inherited Clearance Value, then a clearance should be inserted at the same level as the Inherited Clearance Value
		// (or the Contract level if no Inherited Clearance Value exists) with the same territories scope as the Inherited Clearance Value,
		if (rightOne.isUs()) {
			DigitalRightsEntityUS saved = digitalRightsUSRepository.save(DigitalRightsEntityUS.builder()
				.nodeInstanceId(toBeInserted.getNodeInstanceId())
				.cleared(toBeInserted.getClearanceValue() == ClearanceValue.YES)
				.draRightsHierarchyId(PERPETUAL_HIERARCHY_ID)
				.territory(toBeInserted.getTerritory())
				.modStamp(LocalDateTime.now())
				.modUser(ADMIN_USER)
				.statusFlag(true)
				.build());
			digitalRightsUs = saved.getId();
			createAndSaveProtocol(actionId, rightOne, toBeInserted.getNodeInstanceId(), digitalRightsEu, digitalRightsUs, ResultStatus.SUCCESS, null);
			inputRightsUSRepository.updateProcessed(rightOne.getId());
		} else {
			DigitalRightsEntityEU saved = digitalRightsEURepository.save(DigitalRightsEntityEU.builder()
				.nodeInstanceId(toBeInserted.getNodeInstanceId())
				.approvalTerm(toBeInserted.getClearanceValue() == ClearanceValue.YES)
				.draRightsHierarchyId(PERPETUAL_HIERARCHY_ID)
				.territory(toBeInserted.getTerritory())
				.modStamp(LocalDateTime.now())
				.modUser(ADMIN_USER)
				.statusFlag(true)
				.build());
			digitalRightsEu = saved.getId();
			createAndSaveProtocol(actionId, rightOne, toBeInserted.getNodeInstanceId(), digitalRightsEu, digitalRightsUs, ResultStatus.SUCCESS, null);
			inputRightsEURepository.updateProcessed(rightOne.getId());
		}
		//skip the other rights for the same recording
		instanceIdToBeMigrated.add(toBeInserted);
		rights.remove(rightOne);
		String text = String.format(NODE_INSTANCE_HAS_ALREADY_BEEN_MIGRATED_MULTIPLE, toBeInserted.getNodeInstanceId());
		createSkippedProtocolEntry(rights, actionId, digitalRightsInstanceId, ResultStatus.SKIPPED, text);

		statistics.addRelatedContractId(rightOne.getContractId());
		statistics.incrementDigitalRightsCount(1);
		statistics.incrementSkippedCount(rights.size());
		log.info("Migrating Perpetual Right for contract {} on the node_instance {}  has been finished.", rightOne.getContractNumber(),
			toBeInserted.getNodeInstanceId().toString());
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

	public DigitalRightsNodeInstanceAndClearanceData existingPerpetualRightsContainsNodeInstance(List<DigitalRightsNodeInstanceAndClearanceData> existingPerpetualRights,
		DigitalRightsToBeInserted toBeInserted) {
		return existingPerpetualRights.stream().filter(item -> Objects.equals(item.getNodeInstanceId(), toBeInserted.getNodeInstanceId())).findFirst().orElse(null);
	}

	public DigitalRightsToBeInserted migratedPerpetualRightsContainsNodeInstance(Set<DigitalRightsToBeInserted> existingMigratedPerpetualRights,
		DigitalRightsToBeInserted toBeInserted) {
		return existingMigratedPerpetualRights.stream().filter(item -> Objects.equals(item.getNodeInstanceId(), toBeInserted.getNodeInstanceId())).findFirst().orElse(null);

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
