package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.dto.CreateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.CompanyResponse;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.entity.Company;
import com.bizee.demo.bizee_demo.entity.RegisteredAgent;
import com.bizee.demo.bizee_demo.event.dto.RegisteredAgentAssignedEvent;
import com.bizee.demo.bizee_demo.event.dto.StateCapacityThresholdReachedEvent;
import com.bizee.demo.bizee_demo.event.publish.RegisteredAgentAssignedEventPublisher;
import com.bizee.demo.bizee_demo.event.publish.StateCapacityThresholdReachedEventPublisher;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.repository.CompanyRepository;
import com.bizee.demo.bizee_demo.repository.RegisteredAgentRepository;
import com.bizee.demo.bizee_demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

	@Mock
	private CompanyRepository companyRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RegisteredAgentRepository registeredAgentRepository;

	@Mock
	private RegisteredAgentAssignedEventPublisher registeredAgentAssignedEventPublisher;

	@Mock
	private StateCapacityThresholdReachedEventPublisher stateCapacityThresholdReachedEventPublisher;

	@InjectMocks
	private CompanyService companyService;

	@Test
	void createCompany_withService_assignsAgentWithFewestAssignments() {
		when(userRepository.existsById(1L)).thenReturn(true);

		RegisteredAgent lighterLoad = agent(10L, "CA", 8, "Light Agent", "light@example.com");
		RegisteredAgent heavierLoad = agent(11L, "CA", 12, "Heavy Agent", "heavy@example.com");

		// Repository returns equal-load-balanced order: fewest assigned first, then lowest id.
		when(registeredAgentRepository.findAvailableByStateOrderByAssignedCountAsc("CA", null))
				.thenReturn(List.of(lighterLoad, heavierLoad));
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
			Company company = invocation.getArgument(0);
			company.setId(100L);
			return company;
		});
		when(registeredAgentRepository.findById(10L)).thenReturn(Optional.of(lighterLoad));
		when(registeredAgentRepository.sumTotalCapacityByState("CA")).thenReturn(20L);
		when(companyRepository.countServiceAssignedByState("CA")).thenReturn(2L);

		CompanyResponse response = companyService.createCompany(
				1L,
				new CreateCompanyRequest("Acme LLC", "ca", true)
		);

		assertEquals(100L, response.id());
		assertEquals("CA", response.state());
		assertEquals(RegisteredAgentType.REGISTERED_AGENT, response.registeredAgentType());
		assertEquals(10L, response.registeredAgentId());
		verify(registeredAgentRepository).findAvailableByStateOrderByAssignedCountAsc(eq("CA"), isNull());

		ArgumentCaptor<RegisteredAgentAssignedEvent> assignedCaptor =
				ArgumentCaptor.forClass(RegisteredAgentAssignedEvent.class);
		verify(registeredAgentAssignedEventPublisher).publish(assignedCaptor.capture());
		assertEquals(100L, assignedCaptor.getValue().companyId());
		assertEquals(10L, assignedCaptor.getValue().registeredAgentId());
		assertEquals("light@example.com", assignedCaptor.getValue().registeredAgentEmail());
		verify(stateCapacityThresholdReachedEventPublisher, never()).publish(any());
	}

	@Test
	void createCompany_withService_publishesCapacityThresholdWhenAtOrAbove90Percent() {
		when(userRepository.existsById(1L)).thenReturn(true);

		RegisteredAgent agent = agent(10L, "WY", 10, "Wyoming Agent", "agent.wy@example.com");
		when(registeredAgentRepository.findAvailableByStateOrderByAssignedCountAsc("WY", null))
				.thenReturn(List.of(agent));
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
			Company company = invocation.getArgument(0);
			company.setId(200L);
			return company;
		});
		when(registeredAgentRepository.findById(10L)).thenReturn(Optional.of(agent));
		when(registeredAgentRepository.sumTotalCapacityByState("WY")).thenReturn(10L);
		when(companyRepository.countServiceAssignedByState("WY")).thenReturn(9L);

		companyService.createCompany(1L, new CreateCompanyRequest("Threshold Co", "WY", true));

		ArgumentCaptor<StateCapacityThresholdReachedEvent> thresholdCaptor =
				ArgumentCaptor.forClass(StateCapacityThresholdReachedEvent.class);
		verify(stateCapacityThresholdReachedEventPublisher).publish(thresholdCaptor.capture());
		assertEquals("WY", thresholdCaptor.getValue().state());
		assertEquals(9L, thresholdCaptor.getValue().usedCapacity());
		assertEquals(10L, thresholdCaptor.getValue().totalCapacity());
		assertEquals(90.0, thresholdCaptor.getValue().utilizationPercent());
		assertEquals("admin@bizee.test", thresholdCaptor.getValue().adminEmail());
	}

	@Test
	void createCompany_asSelf_assignsUserAsRegisteredAgent() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
			Company company = invocation.getArgument(0);
			company.setId(101L);
			return company;
		});

		CompanyResponse response = companyService.createCompany(
				1L,
				new CreateCompanyRequest("Self RA Co", "NY", false)
		);

		assertEquals(RegisteredAgentType.USER, response.registeredAgentType());
		assertEquals(1L, response.registeredAgentId());
		verify(registeredAgentRepository, never())
				.findAvailableByStateOrderByAssignedCountAsc(any(), any());
		verify(registeredAgentAssignedEventPublisher, never()).publish(any());
		verify(stateCapacityThresholdReachedEventPublisher, never()).publish(any());
	}

	@Test
	void createCompany_serviceRequestedWithoutCapacity_throwsBusinessException() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(registeredAgentRepository.findAvailableByStateOrderByAssignedCountAsc("IL", null))
				.thenReturn(List.of());

		assertThrows(BusinessException.class, () -> companyService.createCompany(
				1L,
				new CreateCompanyRequest("No Capacity Co", "IL", true)
		));
		verify(registeredAgentAssignedEventPublisher, never()).publish(any());
	}

	@Test
	void updateRegisteredAgent_forOtherUsersCompany_throwsNotFound() {
		when(userRepository.existsById(1L)).thenReturn(true);
		when(companyRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> companyService.updateRegisteredAgent(
				1L,
				2L,
				new UpdateRegisteredAgentRequest(false)
		));
	}

	@Test
	void updateRegisteredAgent_toSelf_updatesOwnedCompany() {
		when(userRepository.existsById(1L)).thenReturn(true);

		Company company = new Company();
		company.setId(2L);
		company.setUserId(1L);
		company.setName("Golden State Analytics Inc");
		company.setState("CA");
		company.setRegisteredAgentType(RegisteredAgentType.REGISTERED_AGENT);
		company.setRegisteredAgentId(5L);

		when(companyRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(company));
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CompanyResponse response = companyService.updateRegisteredAgent(
				1L,
				2L,
				new UpdateRegisteredAgentRequest(false)
		);

		assertEquals(RegisteredAgentType.USER, response.registeredAgentType());
		assertEquals(1L, response.registeredAgentId());

		ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
		verify(companyRepository).save(captor.capture());
		assertEquals(RegisteredAgentType.USER, captor.getValue().getRegisteredAgentType());
		verify(registeredAgentAssignedEventPublisher, never()).publish(any());
	}

	@Test
	void updateRegisteredAgent_toService_excludesCurrentCompanyFromLoadAndBalances() {
		when(userRepository.existsById(1L)).thenReturn(true);

		Company company = new Company();
		company.setId(2L);
		company.setUserId(1L);
		company.setName("Golden State Analytics Inc");
		company.setState("CA");
		company.setRegisteredAgentType(RegisteredAgentType.USER);
		company.setRegisteredAgentId(1L);

		RegisteredAgent balancedAgent = agent(50L, "CA", 12, "CA Agent 2", "agent.ca.2@example.com");

		when(companyRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(company));
		when(registeredAgentRepository.findAvailableByStateOrderByAssignedCountAsc("CA", 2L))
				.thenReturn(List.of(balancedAgent));
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(registeredAgentRepository.findById(50L)).thenReturn(Optional.of(balancedAgent));
		when(registeredAgentRepository.sumTotalCapacityByState("CA")).thenReturn(100L);
		when(companyRepository.countServiceAssignedByState("CA")).thenReturn(2L);

		CompanyResponse response = companyService.updateRegisteredAgent(
				1L,
				2L,
				new UpdateRegisteredAgentRequest(true)
		);

		assertEquals(RegisteredAgentType.REGISTERED_AGENT, response.registeredAgentType());
		assertEquals(50L, response.registeredAgentId());
		verify(registeredAgentRepository).findAvailableByStateOrderByAssignedCountAsc("CA", 2L);
		verify(registeredAgentAssignedEventPublisher).publish(any(RegisteredAgentAssignedEvent.class));
	}

	private static RegisteredAgent agent(Long id, String state, int capacity, String name, String email) {
		RegisteredAgent registeredAgent = new RegisteredAgent();
		registeredAgent.setId(id);
		registeredAgent.setState(state);
		registeredAgent.setCapacity(capacity);
		registeredAgent.setName(name);
		registeredAgent.setEmail(email);
		return registeredAgent;
	}
}
