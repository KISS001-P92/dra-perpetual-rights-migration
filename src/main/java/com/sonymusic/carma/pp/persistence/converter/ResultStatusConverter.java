package com.sonymusic.carma.pp.persistence.converter;

import com.sonymusic.carma.pp.persistence.entity.ResultStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResultStatusConverter implements AttributeConverter<ResultStatus, String> {
	@Override
	public String convertToDatabaseColumn(ResultStatus attribute) {
		return attribute != null ? attribute.name() : null;
	}

	@Override
	public ResultStatus convertToEntityAttribute(String dbData) {
		return dbData != null ? ResultStatus.valueOf(dbData) : null;
	}
}
