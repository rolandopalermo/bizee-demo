package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		@NotBlank
		@Size(max = 150)
		String name,

		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		/**
		 * When blank/null, the existing password is retained.
		 */
		@Size(max = 255)
		String password
) {
}
