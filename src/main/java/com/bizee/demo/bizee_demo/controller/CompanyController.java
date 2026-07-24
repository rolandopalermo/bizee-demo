package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.CompanyResponse;
import com.bizee.demo.bizee_demo.dto.CreateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.UpdateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	private final CompanyService companyService;

	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}

	@GetMapping
	public List<CompanyResponse> listCompanies(@RequestHeader("X-User-Id") Long userId) {
		return companyService.listCompanies(userId);
	}

	@GetMapping("/{companyId}")
	public CompanyResponse getCompany(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long companyId
	) {
		return companyService.getCompany(userId, companyId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CompanyResponse createCompany(
			@RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody CreateCompanyRequest request
	) {
		return companyService.createCompany(userId, request);
	}

	@PutMapping("/{companyId}")
	public CompanyResponse updateCompany(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long companyId,
			@Valid @RequestBody UpdateCompanyRequest request
	) {
		return companyService.updateCompany(userId, companyId, request);
	}

	@PutMapping("/{companyId}/registered-agent")
	public CompanyResponse updateRegisteredAgent(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long companyId,
			@Valid @RequestBody UpdateRegisteredAgentRequest request
	) {
		return companyService.updateRegisteredAgent(userId, companyId, request);
	}

	@DeleteMapping("/{companyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCompany(
			@RequestHeader("X-User-Id") Long userId,
			@PathVariable Long companyId
	) {
		companyService.deleteCompany(userId, companyId);
	}
}
