package com.bizee.demo.bizee_demo.event;

/**
 * Exchange, queue, and routing-key names for domain notification events.
 */
public final class RabbitMqTopology {

	public static final String DOMAIN_EVENTS_EXCHANGE = "bizee.domain.events";

	public static final String REGISTERED_AGENT_ASSIGNED_ROUTING_KEY = "registered-agent.assigned";
	public static final String STATE_CAPACITY_THRESHOLD_ROUTING_KEY = "state.capacity.threshold";

	public static final String REGISTERED_AGENT_ASSIGNED_QUEUE = "bizee.notifications.registered-agent-assigned";
	public static final String STATE_CAPACITY_THRESHOLD_QUEUE = "bizee.notifications.state-capacity-threshold";

	public static final String CAPACITY_THRESHOLD_ADMIN_EMAIL = "admin@bizee.test";
	public static final double CAPACITY_THRESHOLD_PERCENT = 90.0;

	private RabbitMqTopology() {
	}
}
