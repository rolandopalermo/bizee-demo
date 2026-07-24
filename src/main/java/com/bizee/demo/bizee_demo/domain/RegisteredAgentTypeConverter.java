package com.bizee.demo.bizee_demo.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RegisteredAgentTypeConverter implements AttributeConverter<RegisteredAgentType, String> {

	@Override
	public String convertToDatabaseColumn(RegisteredAgentType attribute) {
		return attribute == null ? null : attribute.getValue();
	}

	@Override
	public RegisteredAgentType convertToEntityAttribute(String dbData) {
		return dbData == null ? null : RegisteredAgentType.fromValue(dbData);
	}
}
