package com.bizee.demo.bizee_demo.dto;

import com.bizee.demo.bizee_demo.entity.User;

import java.time.Instant;

public record UserResponse(
		Long id,
		String name,
		String email,
		Instant createdAt,
		Instant updatedAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
