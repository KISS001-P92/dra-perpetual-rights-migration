package com.sonymusic.carma.pp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class MigrationStatistics {
	Long digitalRightsCount = 0L;
	List<String> countryCodes = new ArrayList<>();
	List<Integer> contractIds = new ArrayList<>();
	Long skippedCount = 0L;
	Long failedCount = 0L;
	//	List<Integer> failedValidationContractIds = new ArrayList<>();
	Integer actionId;
	List<Integer> relatedContractIds = new ArrayList<>();

	public MigrationStatistics(Integer actionId) {
		this.actionId = actionId;
	}

	public void incrementDigitalRightsCount(long count) {
		digitalRightsCount += count;
	}

	public void incrementSkippedCount(long count) {
		skippedCount += count;
	}

	public void incrementFailedCount(long count) {
		failedCount += count;
	}

	public void addCountryCodes(List<String> countryCodes) {
		this.countryCodes.addAll(countryCodes);
	}

	public void addContractIds(List<Integer> contractIds) {
		this.contractIds.addAll(contractIds);
	}

	public void addRelatedContractId(Integer contractId) {
		this.relatedContractIds.add(contractId);
	}

	//	public void addFailedValidationContractIds(List<Integer> contractIds) {
	//		this.failedValidationContractIds.addAll(contractIds);
	//	}

	public void merge(MigrationStatistics other) {
		this.digitalRightsCount += other.digitalRightsCount;
		this.countryCodes.addAll(other.countryCodes);
		this.contractIds.addAll(other.contractIds);
		this.skippedCount += other.skippedCount;
		this.failedCount += other.failedCount;
	}

}
