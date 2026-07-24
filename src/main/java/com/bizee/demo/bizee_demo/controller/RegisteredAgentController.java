package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.CreateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentCapacityResponse;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentResponse;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentEntityRequest;
import com.bizee.demo.bizee_demo.service.RegisteredAgentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/registered-agents")
public class RegisteredAgentController {

	private final RegisteredAgentService registeredAgentService;

	public RegisteredAgentController(RegisteredAgentService registeredAgentService) {
		this.registeredAgentService = registeredAgentService;
	}

	@GetMapping
	public List<RegisteredAgentResponse> listAgents() {
		return registeredAgentService.listAgents();
	}

	@GetMapping("/capacity")
	public RegisteredAgentCapacityResponse checkCapacity(
			@RequestParam
			@Pattern(regexp = "^[A-Za-z]{2}$", message = "state must be a 2-letter US state code")
			String state
	) {
		return registeredAgentService.checkCapacity(state);
	}

	@GetMapping("/{id}")
	public RegisteredAgentResponse getAgent(@PathVariable Long id) {
		return registeredAgentService.getAgent(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RegisteredAgentResponse createAgent(@Valid @RequestBody CreateRegisteredAgentRequest request) {
		return registeredAgentService.createAgent(request);
	}

	@PutMapping("/{id}")
	public RegisteredAgentResponse updateAgent(
			@PathVariable Long id,
			@Valid @RequestBody UpdateRegisteredAgentEntityRequest request
	) {
		return registeredAgentService.updateAgent(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAgent(@PathVariable Long id) {
		registeredAgentService.deleteAgent(id);
	}
}
