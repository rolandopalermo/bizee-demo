package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRegisteredAgentRequest(
		@NotBlank
		@Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter US state code")
		String state,

		@NotBlank
		@Size(max = 150)
		String name,

		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		@NotNull
		@Min(5)
		@Max(15)
		Integer capacity
) {
}
