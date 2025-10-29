package com.sonymusic.carma.pp.persistence.converter;

import com.sonymusic.carma.pp.persistence.entity.ApprovalTerm;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApprovalTermConverter implements AttributeConverter<ApprovalTerm, String> {

	@Override
	public String convertToDatabaseColumn(ApprovalTerm value) {
		return value != null ? value.getValue() : null;
	}

	@Override
	public ApprovalTerm convertToEntityAttribute(String value) {
		return ApprovalTerm.fromValue(value);
	}
}
