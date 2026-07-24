package com.bizee.demo.bizee_demo.dto;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.entity.Company;
import com.bizee.demo.bizee_demo.entity.User;

import java.time.Instant;

public record CompanyResponse(
		Long id,
		Long userId,
		String userName,
		String userEmail,
		String name,
		String state,
		RegisteredAgentType registeredAgentType,
		Long registeredAgentId,
		Instant createdAt,
		Instant updatedAt
) {

	public static CompanyResponse from(Company company) {
		return from(company, null);
	}

	public static CompanyResponse from(Company company, User owner) {
		return new CompanyResponse(
				company.getId(),
				company.getUserId(),
				owner != null ? owner.getName() : null,
				owner != null ? owner.getEmail() : null,
				company.getName(),
				company.getState(),
				company.getRegisteredAgentType(),
				company.getRegisteredAgentId(),
				company.getCreatedAt(),
				company.getUpdatedAt()
		);
	}
}
