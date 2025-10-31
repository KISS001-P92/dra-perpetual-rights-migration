package com.sonymusic.carma.pp.persistence.converter;

import jakarta.persistence.AttributeConverter;
import org.apache.commons.lang3.StringUtils;

public class ClearanceValueConverter implements AttributeConverter<ClearanceValue, String> {
	@Override
	public String convertToDatabaseColumn(ClearanceValue attribute) {
		return attribute == null ? null : (ClearanceValue.YES == attribute ? "Y" : "N");
	}

	@Override
	public ClearanceValue convertToEntityAttribute(String dbData) {
		return dbData == null ? null : ("Y".equals(dbData) ? ClearanceValue.YES : ClearanceValue.NO);
	}

	public ClearanceValue convertToEntityAttributeFromInput(String value) {
		return StringUtils.isEmpty(value) ?
			null :
			("Y".equals(value) || "YES".equals(value)) ? ClearanceValue.YES
				: ("N".equals(value) || "NO".equals(value) ? ClearanceValue.NO : ClearanceValue.INVALID);
	}
}
