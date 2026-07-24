package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRegisteredAgentRequest(
		@NotNull
		Boolean useRegisteredAgentService
) {
}
