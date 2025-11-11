package com.sonymusic.carma.pp.persistence.entity;

import com.sonymusic.carma.pp.persistence.converter.ClearanceValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InheritedAndIntendedData {
	private ClearanceValue inheritedClearance;
	private DigitalRightsToBeInserted intendedData;
}
