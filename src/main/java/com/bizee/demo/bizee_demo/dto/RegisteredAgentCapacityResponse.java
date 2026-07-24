package com.bizee.demo.bizee_demo.dto;

public record RegisteredAgentCapacityResponse(
		String state,
		boolean available,
		long remainingCapacity
) {
}
