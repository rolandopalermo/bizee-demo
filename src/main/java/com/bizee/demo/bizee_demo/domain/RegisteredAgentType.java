package com.bizee.demo.bizee_demo.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum RegisteredAgentType {
	USER("user"),
	REGISTERED_AGENT("registered_agent");

	private final String value;

	RegisteredAgentType(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	public static RegisteredAgentType fromValue(String value) {
		return Arrays.stream(values())
				.filter(type -> type.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown registered agent type: " + value));
	}
}
