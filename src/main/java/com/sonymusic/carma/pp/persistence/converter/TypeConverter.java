package com.sonymusic.carma.pp.persistence.converter;

import com.sonymusic.carma.pp.persistence.entity.Type;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TypeConverter implements AttributeConverter<Type, String> {

	@Override
	public String convertToDatabaseColumn(Type value) {
		return value != null ? value.getValue() : null;
	}

	@Override
	public Type convertToEntityAttribute(String value) {
		return Type.fromValue(value);
	}
}
