package com.bizee.demo.bizee_demo.event.dto;

/**
 * Domain event: a company was assigned to a registered-agent service agent.
 */
public record RegisteredAgentAssignedEvent(
		Long companyId,
		String companyName,
		String state,
		Long registeredAgentId,
		String registeredAgentName,
		String registeredAgentEmail
) {
}
