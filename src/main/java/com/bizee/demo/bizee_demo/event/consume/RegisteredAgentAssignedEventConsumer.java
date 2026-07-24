package com.bizee.demo.bizee_demo.event.consume;

import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import com.bizee.demo.bizee_demo.event.dto.RegisteredAgentAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Handles side effects for {@link RegisteredAgentAssignedEvent}.
 * Does not send real email — logs a simulated notification to the console.
 */
@Service
public class RegisteredAgentAssignedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(RegisteredAgentAssignedEventConsumer.class);

	@RabbitListener(queues = RabbitMqTopology.REGISTERED_AGENT_ASSIGNED_QUEUE)
	public void onRegisteredAgentAssigned(RegisteredAgentAssignedEvent event) {
		log.info(
				"MAIL SENT (simulated): To={} ({}) | Subject=New company assignment | "
						+ "Company id={}, name='{}', state={} | Agent id={}",
				event.registeredAgentEmail(),
				event.registeredAgentName(),
				event.companyId(),
				event.companyName(),
				event.state(),
				event.registeredAgentId()
		);
	}
}
