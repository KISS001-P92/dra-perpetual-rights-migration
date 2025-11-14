package com.sonymusic.carma.pp.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InheritedAndIntendedData {
	private boolean isSkipped;
	private ClearanceValue inheritedClearance;
	private DigitalRightsToBeInserted intendedData;
}
