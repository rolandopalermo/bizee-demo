package com.bizee.demo.bizee_demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Thymeleaf form backing object for company create/edit.
 * Owner is the logged-in session user on create (not selected in the form).
 */
public class CompanyForm {

	@NotBlank
	@Size(max = 200)
	private String name;

	@NotBlank
	@Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter US state code")
	private String state;

	@NotNull
	private Boolean useRegisteredAgentService = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Boolean getUseRegisteredAgentService() {
		return useRegisteredAgentService;
	}

	public void setUseRegisteredAgentService(Boolean useRegisteredAgentService) {
		this.useRegisteredAgentService = useRegisteredAgentService;
	}
}
