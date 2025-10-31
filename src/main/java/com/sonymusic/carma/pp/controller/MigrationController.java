package com.sonymusic.carma.pp.controller;

import com.sonymusic.carma.pp.model.MigrationConfiguration;
import com.sonymusic.carma.pp.service.MigrationService;
import com.sonymusic.carma.pp.service.SendToDraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

	private final MigrationService migrationService;
	private final SendToDraService sendToDraService;

	private static final String SUCCESS = "success";

	@PostMapping(value = "/start-migration", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> startMigration(@RequestBody MigrationConfiguration configuration) {
		log.info("Migration started with the following configuration: {}", configuration);
		if (configuration.hasCountryKeys()) {
			migrationService.migrateByCountry(configuration.getCountryKeys());
		}
		if (configuration.hasContractIds()) {
			migrationService.migrateByContract(configuration.getContractIds());
		}
		return ResponseEntity.ok(SUCCESS);
	}

	@PostMapping(value = "/clear-migrated-data", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> clearMigratedData(@RequestBody MigrationConfiguration configuration) {
		log.info("Clear migrated data started with the following configuration: {}", configuration);
		if (configuration.hasCountryKeys()) {
			migrationService.clearByCountry(configuration.getCountryKeys());
		}
		if (configuration.hasContractIds()) {
			migrationService.clearByContract(configuration.getContractIds());
		}
		return ResponseEntity.ok(SUCCESS);
	}

	@PostMapping(value = "/send-to-dra", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> sendToDra(@RequestBody MigrationConfiguration configuration) {
		log.info("Sending data to DRA started with the following configuration: {}", configuration);
		if (configuration.hasCountryKeys()) {
			sendToDraService.sendToDraByCountryCodes(configuration.getCountryKeys());
		}
		if (configuration.hasContractIds()) {
			sendToDraService.sendToDraByContractsIds(configuration.getContractIds());
		}
		return ResponseEntity.ok(SUCCESS);
	}
}
