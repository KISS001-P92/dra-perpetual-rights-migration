package com.sonymusic.carma.pp.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ADConverter implements AttributeConverter<Boolean, String> {
	@Override
	public String convertToDatabaseColumn(Boolean attribute) {
		return attribute != null ? (attribute ? "A" : "D") : null;
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		return dbData != null ? ("A".equals(dbData)) : null;
	}
}
