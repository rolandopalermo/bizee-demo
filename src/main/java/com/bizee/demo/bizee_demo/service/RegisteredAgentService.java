package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.dto.CreateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentCapacityResponse;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentResponse;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentEntityRequest;
import com.bizee.demo.bizee_demo.entity.RegisteredAgent;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.repository.CompanyRepository;
import com.bizee.demo.bizee_demo.repository.RegisteredAgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class RegisteredAgentService {

	private final RegisteredAgentRepository registeredAgentRepository;
	private final CompanyRepository companyRepository;

	public RegisteredAgentService(
			RegisteredAgentRepository registeredAgentRepository,
			CompanyRepository companyRepository
	) {
		this.registeredAgentRepository = registeredAgentRepository;
		this.companyRepository = companyRepository;
	}

	@Transactional(readOnly = true)
	public List<RegisteredAgentResponse> listAgents() {
		return listAgents(null);
	}

	/**
	 * Lists agents ordered by state then name, optionally filtered by state code.
	 */
	@Transactional(readOnly = true)
	public List<RegisteredAgentResponse> listAgents(String stateFilter) {
		List<RegisteredAgent> agents;
		if (stateFilter == null || stateFilter.isBlank()) {
			agents = registeredAgentRepository.findAllByOrderByStateAscNameAsc();
		}
		else {
			String state = normalizeState(stateFilter);
			agents = registeredAgentRepository.findByStateIgnoreCaseOrderByNameAsc(state);
		}
		return agents.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public RegisteredAgentResponse getAgent(Long id) {
		return toResponse(requireAgent(id));
	}

	@Transactional
	public RegisteredAgentResponse createAgent(CreateRegisteredAgentRequest request) {
		String state = normalizeState(request.state());
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email, null);

		RegisteredAgent agent = new RegisteredAgent();
		agent.setState(state);
		agent.setName(request.name().trim());
		agent.setEmail(email);
		agent.setCapacity(request.capacity());

		return RegisteredAgentResponse.from(registeredAgentRepository.save(agent), 0L);
	}

	@Transactional
	public RegisteredAgentResponse updateAgent(Long id, UpdateRegisteredAgentEntityRequest request) {
		RegisteredAgent agent = requireAgent(id);
		String state = normalizeState(request.state());
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email, id);

		long assigned = assignedCompanyCount(id);
		if (request.capacity() < assigned) {
			throw new BusinessException(
					"Cannot set capacity to " + request.capacity()
							+ "; agent id=" + id + " already has " + assigned + " assigned companies");
		}

		agent.setState(state);
		agent.setName(request.name().trim());
		agent.setEmail(email);
		agent.setCapacity(request.capacity());

		return RegisteredAgentResponse.from(registeredAgentRepository.save(agent), assigned);
	}

	@Transactional
	public void deleteAgent(Long id) {
		RegisteredAgent agent = requireAgent(id);
		long assigned = assignedCompanyCount(id);
		if (assigned > 0) {
			throw new BusinessException(
					"Cannot delete registered agent id=" + id
							+ "; " + assigned + " companies still use this agent");
		}
		registeredAgentRepository.delete(agent);
	}

	@Transactional(readOnly = true)
	public RegisteredAgentCapacityResponse checkCapacity(String state) {
		String normalizedState = normalizeState(state);
		long remaining = registeredAgentRepository.sumRemainingCapacityByState(normalizedState);
		return new RegisteredAgentCapacityResponse(normalizedState, remaining > 0, remaining);
	}

	private RegisteredAgentResponse toResponse(RegisteredAgent agent) {
		return RegisteredAgentResponse.from(agent, assignedCompanyCount(agent.getId()));
	}

	private long assignedCompanyCount(Long agentId) {
		return companyRepository.countByRegisteredAgentTypeAndRegisteredAgentId(
				RegisteredAgentType.REGISTERED_AGENT,
				agentId
		);
	}

	private RegisteredAgent requireAgent(Long id) {
		return registeredAgentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Registered agent not found for id=" + id));
	}

	private void ensureEmailAvailable(String email, Long excludeId) {
		boolean taken = excludeId == null
				? registeredAgentRepository.existsByEmailIgnoreCase(email)
				: registeredAgentRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
		if (taken) {
			throw new BusinessException("Registered agent email already exists: " + email);
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.US);
	}

	private String normalizeState(String state) {
		if (state == null || state.isBlank()) {
			throw new BusinessException("state is required");
		}
		String normalized = state.trim().toUpperCase(Locale.US);
		if (normalized.length() != 2) {
			throw new BusinessException("state must be a 2-letter US state code");
		}
		return normalized;
	}
}
