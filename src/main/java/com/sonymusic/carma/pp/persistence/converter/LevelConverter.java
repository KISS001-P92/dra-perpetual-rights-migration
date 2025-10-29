package com.sonymusic.carma.pp.persistence.converter;

import com.sonymusic.carma.pp.persistence.entity.Level;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LevelConverter implements AttributeConverter<Level, String> {

	@Override
	public String convertToDatabaseColumn(Level value) {
		return value != null ? value.name() : null;
	}

	@Override
	public Level convertToEntityAttribute(String value) {
		return value != null ? Level.valueOf(value) : null;
	}
}
