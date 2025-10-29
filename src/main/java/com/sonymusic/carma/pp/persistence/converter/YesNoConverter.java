package com.sonymusic.carma.pp.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class YesNoConverter implements AttributeConverter<Boolean, String> {
	@Override
	public String convertToDatabaseColumn(Boolean attribute) {
		return attribute != null ? (attribute ? "Y" : "N") : null;
	}

	@Override
	public Boolean convertToEntityAttribute(String dbData) {
		return dbData != null ? ("Y".equals(dbData)) : null;
	}
}
