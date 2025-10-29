package com.sonymusic.carma.pp.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SendingDraStatistics {

	List<String> countryCodes = new ArrayList<>();
	List<Integer> contractIds = new ArrayList<>();
	Integer skippedCount = 0;
	Integer sentCount = 0;

	public void addCountryCodes(List<String> countryCodes) {
		this.countryCodes.addAll(countryCodes);
	}

	public void addContractIds(List<Integer> contractIds) {
		this.contractIds.addAll(contractIds);
	}

}
