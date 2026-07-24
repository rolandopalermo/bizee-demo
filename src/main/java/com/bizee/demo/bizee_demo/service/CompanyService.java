package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.dto.CompanyResponse;
import com.bizee.demo.bizee_demo.dto.CreateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.UpdateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.entity.Company;
import com.bizee.demo.bizee_demo.entity.RegisteredAgent;
import com.bizee.demo.bizee_demo.entity.User;
import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import com.bizee.demo.bizee_demo.event.dto.RegisteredAgentAssignedEvent;
import com.bizee.demo.bizee_demo.event.dto.StateCapacityThresholdReachedEvent;
import com.bizee.demo.bizee_demo.event.publish.RegisteredAgentAssignedEventPublisher;
import com.bizee.demo.bizee_demo.event.publish.StateCapacityThresholdReachedEventPublisher;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.repository.CompanyRepository;
import com.bizee.demo.bizee_demo.repository.RegisteredAgentRepository;
import com.bizee.demo.bizee_demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final RegisteredAgentRepository registeredAgentRepository;
	private final RegisteredAgentAssignedEventPublisher registeredAgentAssignedEventPublisher;
	private final StateCapacityThresholdReachedEventPublisher stateCapacityThresholdReachedEventPublisher;

	private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

	public CompanyService(
			CompanyRepository companyRepository,
			UserRepository userRepository,
			RegisteredAgentRepository registeredAgentRepository,
			RegisteredAgentAssignedEventPublisher registeredAgentAssignedEventPublisher,
			StateCapacityThresholdReachedEventPublisher stateCapacityThresholdReachedEventPublisher
	) {
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
		this.registeredAgentRepository = registeredAgentRepository;
		this.registeredAgentAssignedEventPublisher = registeredAgentAssignedEventPublisher;
		this.stateCapacityThresholdReachedEventPublisher = stateCapacityThresholdReachedEventPublisher;
	}

	/**
	 * Lists companies for REST ({@code X-User-Id} ownership scope).
	 */
	@Transactional(readOnly = true)
	public List<CompanyResponse> listCompanies(Long userId) {
		requireUser(userId);
		return toResponses(companyRepository.findByUserIdOrderByIdAsc(userId));
	}

	/**
	 * Lists all companies, optionally filtered by owner. Used by the Thymeleaf UI.
	 */
	@Transactional(readOnly = true)
	public List<CompanyResponse> listAllCompanies(Long filterUserId) {
		if (filterUserId != null) {
			requireUser(filterUserId);
			return toResponses(companyRepository.findByUserIdOrderByIdAsc(filterUserId));
		}
		return toResponses(companyRepository.findAllByOrderByIdAsc());
	}

	@Transactional(readOnly = true)
	public CompanyResponse getCompany(Long userId, Long companyId) {
		requireUser(userId);
		return toResponse(requireOwnedCompany(userId, companyId));
	}

	/**
	 * Loads a company by id without ownership check (browser list/edit flows).
	 */
	@Transactional(readOnly = true)
	public CompanyResponse getCompanyById(Long companyId) {
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company not found for id=" + companyId));
		return toResponse(company);
	}

	@Transactional
	public CompanyResponse createCompany(Long userId, CreateCompanyRequest request) {
		requireUser(userId);
		String state = normalizeState(request.state());

		Company company = new Company();
		company.setUserId(userId);
		company.setName(request.name().trim());
		company.setState(state);

		assignRegisteredAgent(
				company,
				userId,
				state,
				Boolean.TRUE.equals(request.useRegisteredAgentService()),
				null
		);

		Company saved = companyRepository.save(company);
		publishServiceAssignmentEventsAfterCommit(saved);
		return toResponse(saved);
	}

	@Transactional
	public CompanyResponse updateCompany(Long userId, Long companyId, UpdateCompanyRequest request) {
		requireUser(userId);
		Company company = requireOwnedCompany(userId, companyId);
		String state = normalizeState(request.state());

		company.setName(request.name().trim());
		company.setState(state);

		assignRegisteredAgent(
				company,
				userId,
				state,
				Boolean.TRUE.equals(request.useRegisteredAgentService()),
				company.getId()
		);

		Company saved = companyRepository.save(company);
		publishServiceAssignmentEventsAfterCommit(saved);
		return toResponse(saved);
	}

	@Transactional
	public CompanyResponse updateRegisteredAgent(Long userId, Long companyId, UpdateRegisteredAgentRequest request) {
		requireUser(userId);
		Company company = requireOwnedCompany(userId, companyId);

		assignRegisteredAgent(
				company,
				userId,
				company.getState(),
				Boolean.TRUE.equals(request.useRegisteredAgentService()),
				company.getId()
		);

		Company saved = companyRepository.save(company);
		publishServiceAssignmentEventsAfterCommit(saved);
		return toResponse(saved);
	}

	@Transactional
	public void deleteCompany(Long userId, Long companyId) {
		requireUser(userId);
		Company company = requireOwnedCompany(userId, companyId);
		companyRepository.delete(company);
	}

	private void assignRegisteredAgent(
			Company company,
			Long userId,
			String state,
			boolean useService,
			Long excludeCompanyId
	) {
		if (useService) {
			RegisteredAgent agent = findAvailableAgent(state, excludeCompanyId)
					.orElseThrow(() -> new BusinessException(
							"Registered agent service has no available capacity in state " + state
									+ ". Assign yourself as the registered agent instead."));
			company.setRegisteredAgentType(RegisteredAgentType.REGISTERED_AGENT);
			company.setRegisteredAgentId(agent.getId());
			return;
		}

		company.setRegisteredAgentType(RegisteredAgentType.USER);
		company.setRegisteredAgentId(userId);
	}

	/**
	 * After a successful service-agent assignment (and commit), publish domain events.
	 * Capacity threshold events are published on every assignment that leaves utilization
	 * at or above {@link RabbitMqTopology#CAPACITY_THRESHOLD_PERCENT}% (may repeat while at threshold).
	 */
	private void publishServiceAssignmentEventsAfterCommit(Company company) {
		if (company.getRegisteredAgentType() != RegisteredAgentType.REGISTERED_AGENT) {
			return;
		}

		RegisteredAgent agent = registeredAgentRepository.findById(company.getRegisteredAgentId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Registered agent not found for id=" + company.getRegisteredAgentId()));

		RegisteredAgentAssignedEvent assignedEvent = new RegisteredAgentAssignedEvent(
				company.getId(),
				company.getName(),
				company.getState(),
				agent.getId(),
				agent.getName(),
				agent.getEmail()
		);

		StateCapacityThresholdReachedEvent thresholdEvent = buildThresholdEventIfReached(company.getState());

		runAfterCommit(() -> {
			registeredAgentAssignedEventPublisher.publish(assignedEvent);
			if (thresholdEvent != null) {
				stateCapacityThresholdReachedEventPublisher.publish(thresholdEvent);
			}
		});
	}

	/**
	 * Utilization = (companies in state with service RA) / (sum of RA capacities in state) * 100.
	 * Returns an event when utilization &gt;= 90%; otherwise {@code null}.
	 */
	private StateCapacityThresholdReachedEvent buildThresholdEventIfReached(String state) {
		long totalCapacity = registeredAgentRepository.sumTotalCapacityByState(state);
		if (totalCapacity <= 0) {
			return null;
		}
		long usedCapacity = companyRepository.countServiceAssignedByState(state);
		double utilizationPercent = (usedCapacity * 100.0) / totalCapacity;
		log.info("UtilizationPercent = {}" , utilizationPercent);
		if (utilizationPercent < RabbitMqTopology.CAPACITY_THRESHOLD_PERCENT) {
			return null;
		}
		return new StateCapacityThresholdReachedEvent(
				state,
				usedCapacity,
				totalCapacity,
				utilizationPercent,
				RabbitMqTopology.CAPACITY_THRESHOLD_ADMIN_EMAIL
		);
	}

	private void runAfterCommit(Runnable action) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					action.run();
				}
			});
			return;
		}
		action.run();
	}

	/**
	 * Picks the agent with the fewest assigned companies among those with remaining capacity.
	 * Ties break on lowest agent id (stable). See {@link RegisteredAgentRepository#findAvailableByStateOrderByAssignedCountAsc}.
	 */
	private Optional<RegisteredAgent> findAvailableAgent(String state, Long excludeCompanyId) {
		List<RegisteredAgent> available =
				registeredAgentRepository.findAvailableByStateOrderByAssignedCountAsc(state, excludeCompanyId);
		return available.stream().findFirst();
	}

	private Company requireOwnedCompany(Long userId, Long companyId) {
		return companyRepository.findByIdAndUserId(companyId, userId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Company not found for id=" + companyId + " and userId=" + userId));
	}

	private void requireUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User not found for id=" + userId);
		}
	}

	private List<CompanyResponse> toResponses(List<Company> companies) {
		if (companies.isEmpty()) {
			return List.of();
		}
		Map<Long, User> ownersById = userRepository.findAllById(
						companies.stream().map(Company::getUserId).distinct().toList()
				).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
		return companies.stream()
				.map(company -> CompanyResponse.from(company, ownersById.get(company.getUserId())))
				.toList();
	}

	private CompanyResponse toResponse(Company company) {
		User owner = userRepository.findById(company.getUserId()).orElse(null);
		return CompanyResponse.from(company, owner);
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
