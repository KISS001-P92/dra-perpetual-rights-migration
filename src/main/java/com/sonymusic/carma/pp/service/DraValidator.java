package com.sonymusic.carma.pp.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DraValidator {

	@Getter
	@AllArgsConstructor
	public static class ValidatorInput {
		private Integer contractId;
		private Integer userId;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Response {
		private boolean success;
		private String msg;
	}

	public boolean validateDigitalRights(String url, Integer contractId, Integer userId) {
		return sendRequest(url + "/validate", contractId, userId);
	}

	public boolean deleteValidations(String url, Integer contractId, Integer userId) {
		return sendRequest(url + "/delete-validations", contractId, userId);
	}

	private boolean sendRequest(String url, Integer contractId, Integer userId) {
		if (contractId == null || userId == null) {
			log.info("Skip validation because contractId or userId is null");
			return true;
		}

		RestTemplate restTemplate = new RestTemplate();
		ValidatorInput request = new ValidatorInput(contractId, userId);
		Response response = restTemplate.postForObject(url, request, Response.class);
		if (response == null || !response.isSuccess()) {
			log.error("Digital rights validation failed for contractId=" + contractId);
			return false;
		}
		return true;
	}
}
