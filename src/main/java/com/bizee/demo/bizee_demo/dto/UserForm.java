package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Thymeleaf form backing object for user create/edit.
 */
public class UserForm {

	@NotBlank
	@Size(max = 150)
	private String name;

	@NotBlank
	@Email
	@Size(max = 255)
	private String email;

	@Size(max = 255)
	private String password;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
