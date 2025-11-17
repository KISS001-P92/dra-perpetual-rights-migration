package com.sonymusic.carma.pp.persistence.converter;

import com.sonymusic.carma.pp.persistence.entity.ClearanceValue;
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

	public static ClearanceValue convertToEntityAttributeFromInput(String value) {
		value = StringUtils.trim(value);
		return StringUtils.isEmpty(value) ?
			null :
			("Y".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value)) ? ClearanceValue.YES
				: ("N".equalsIgnoreCase(value) || "NO".equalsIgnoreCase(value)) ? ClearanceValue.NO
				: "".equalsIgnoreCase(value) ? ClearanceValue.EMPTY : ClearanceValue.INVALID;
	}
}
