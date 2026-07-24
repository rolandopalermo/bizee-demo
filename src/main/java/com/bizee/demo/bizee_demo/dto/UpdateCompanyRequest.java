package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
		@NotBlank
		@Size(max = 200)
		String name,

		@NotBlank
		@Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter US state code")
		String state,

		@NotNull
		Boolean useRegisteredAgentService
) {
}
