package com.sonymusic.carma.pp.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum ApprovalTerm {
	YES("Yes", true),
	NO("No", false),
	EMPTY(null, null);

	private final String value;
	private final Boolean booleanValue;

	public static ApprovalTerm fromValue(String value) {
		return Stream.of(ApprovalTerm.values()).filter(v -> Objects.equals(v.value, value)).findFirst().orElse(EMPTY);
	}
}
