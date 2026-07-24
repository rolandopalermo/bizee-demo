package com.bizee.demo.bizee_demo.event.publish;

import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import com.bizee.demo.bizee_demo.event.dto.RegisteredAgentAssignedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RegisteredAgentAssignedEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(RegisteredAgentAssignedEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public RegisteredAgentAssignedEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(RegisteredAgentAssignedEvent event) {
		log.debug(
				"Publishing RegisteredAgentAssignedEvent companyId={} agentId={}",
				event.companyId(),
				event.registeredAgentId()
		);
		rabbitTemplate.convertAndSend(
				RabbitMqTopology.DOMAIN_EVENTS_EXCHANGE,
				RabbitMqTopology.REGISTERED_AGENT_ASSIGNED_ROUTING_KEY,
				event
		);
	}
}
