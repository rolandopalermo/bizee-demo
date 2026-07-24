package com.bizee.demo.bizee_demo.event.dto;

/**
 * Domain event: registered-agent service utilization for a state is at or above the threshold.
 */
public record StateCapacityThresholdReachedEvent(
		String state,
		long usedCapacity,
		long totalCapacity,
		double utilizationPercent,
		String adminEmail
) {
}
