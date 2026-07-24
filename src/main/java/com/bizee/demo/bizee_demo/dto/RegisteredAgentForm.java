package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Thymeleaf form backing object for registered-agent create/edit.
 */
public class RegisteredAgentForm {

	@NotBlank
	@Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter US state code")
	private String state;

	@NotBlank
	@Size(max = 150)
	private String name;

	@NotBlank
	@Email
	@Size(max = 255)
	private String email;

	@NotNull
	@Min(5)
	@Max(15)
	private Integer capacity = 5;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

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

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
}
