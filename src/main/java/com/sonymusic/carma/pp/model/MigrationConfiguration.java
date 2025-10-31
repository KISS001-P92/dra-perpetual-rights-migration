package com.sonymusic.carma.pp.model;

import lombok.Data;

import java.util.List;

@Data
public class MigrationConfiguration {
	public List<String> countryKeys;
	public List<Integer> contractIds;

	public boolean hasCountryKeys() {
		return countryKeys != null && !countryKeys.isEmpty();
	}

	public boolean hasContractIds() {
		return contractIds != null && !contractIds.isEmpty();
	}
}
