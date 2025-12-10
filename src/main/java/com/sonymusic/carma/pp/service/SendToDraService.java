package com.sonymusic.carma.pp.service;

import com.sonymusic.carma.pp.jms.JMSMessageSender;
import com.sonymusic.carma.pp.model.SendingDraStatistics;
import com.sonymusic.carma.pp.persistence.entity.ContractIdUserExportedPair;
import com.sonymusic.carma.pp.persistence.repository.CarmaRepository;
import com.sonymusic.carma.pp.persistence.repository.ProtocolRepository;
import com.sonymusic.carma.sapi.dra.message.DigitalRightsChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendToDraService {

	private final MailService mailService;

	private final CarmaRepository carmaRepository;
	private final ProtocolRepository protocolRepository;
	private final JMSMessageSender draExportJmsSender;

	public void sendToDraByCountryCodes(List<String> countryCodes) {
		log.info("Sending to DRA started with countries {}", countryCodes);
		Set<Integer> contractIdsToBeSend = protocolRepository.getContractIdsWhereNewDigitalRightExistsByCountryIds(
			new HashSet<>(countryCodes));
		List<ContractIdUserExportedPair> contractIdUserExportedPairs = carmaRepository.getContractIdUserExportedPairByContractIds(contractIdsToBeSend);
		SendingDraStatistics statistics = sendDataToDRA(contractIdUserExportedPairs);
		statistics.addCountryCodes(countryCodes);
		mailService.sendStatusMailForSending(statistics);
		log.info("Sending to DRA completed with countries {}", countryCodes);
	}

	public void sendToDraByContractsIds(List<Integer> contractIds) {
		log.info("Sending to DRA started with contracts {}", contractIds);
		Set<Integer> contractIdsToBeSend = protocolRepository.getContractIdsWhereNewDigitalRightExistsByContractIds(
			new HashSet<>(contractIds));
		List<ContractIdUserExportedPair> contractIdUserExportedPairs = carmaRepository.getContractIdUserExportedPairByContractIds(contractIdsToBeSend);
		SendingDraStatistics statistics = sendDataToDRA(contractIdUserExportedPairs);
		mailService.sendStatusMailForSending(statistics);
		log.info("Sending to DRA completed with contracts {}", contractIds);
	}

	private SendingDraStatistics sendDataToDRA(List<ContractIdUserExportedPair> contractIds) {
		SendingDraStatistics statistics = new SendingDraStatistics();
		if (!contractIds.isEmpty()) {
			List<Integer> userExportedContractIds = contractIds.stream().filter(item -> "Y".equals(item.getIsUserExported())).map(ContractIdUserExportedPair::getContractId)
				.collect(Collectors.toList());
			List<Integer> skippedContractIds = contractIds.stream().filter(item -> "N".equals(item.getIsUserExported())).map(ContractIdUserExportedPair::getContractId)
				.toList();
			statistics.addContractIds(userExportedContractIds);
			statistics.setSkippedCount(skippedContractIds.size());
			statistics.setSentCount(userExportedContractIds.size());
			userExportedContractIds.forEach(contractId ->
				draExportJmsSender.sendMessage(new DigitalRightsChangedMessage(contractId, MigrationService.MOD_USER))
			);
		}
		return statistics;
	}

}
