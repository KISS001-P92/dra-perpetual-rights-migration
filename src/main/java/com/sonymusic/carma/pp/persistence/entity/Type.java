package com.sonymusic.carma.pp.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum Type {
	AUDIO("Audio"),
	VIDEO("Video");

	private final String value;

	public static Type fromValue(String value) {
		return Stream.of(Type.values()).filter(t -> t.value.equals(value)).findFirst().orElse(null);
	}
}
