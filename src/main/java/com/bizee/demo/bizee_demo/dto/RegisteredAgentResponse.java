package com.bizee.demo.bizee_demo.dto;

import com.bizee.demo.bizee_demo.entity.RegisteredAgent;

import java.time.Instant;

public record RegisteredAgentResponse(
		Long id,
		String state,
		String name,
		String email,
		Integer capacity,
		long assignedCount,
		Instant createdAt,
		Instant updatedAt
) {

	public static RegisteredAgentResponse from(RegisteredAgent agent, long assignedCount) {
		return new RegisteredAgentResponse(
				agent.getId(),
				agent.getState(),
				agent.getName(),
				agent.getEmail(),
				agent.getCapacity(),
				assignedCount,
				agent.getCreatedAt(),
				agent.getUpdatedAt()
		);
	}

	/**
	 * CSS class for the capacity utilization square.
	 * Green ≤30%, yellow 31–60%, red &gt;60% (or when total capacity is 0).
	 */
	public String capacityColorClass() {
		int total = capacity == null ? 0 : capacity;
		if (total <= 0) {
			return "capacity-red";
		}
		double pct = (assignedCount * 100.0) / total;
		if (pct <= 30.0) {
			return "capacity-green";
		}
		if (pct <= 60.0) {
			return "capacity-yellow";
		}
		return "capacity-red";
	}
}
