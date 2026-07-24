package com.bizee.demo.bizee_demo.event.publish;

import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import com.bizee.demo.bizee_demo.event.dto.StateCapacityThresholdReachedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class StateCapacityThresholdReachedEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(StateCapacityThresholdReachedEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public StateCapacityThresholdReachedEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(StateCapacityThresholdReachedEvent event) {
		log.debug(
				"Publishing StateCapacityThresholdReachedEvent state={} utilization={}%",
				event.state(),
				event.utilizationPercent()
		);
		rabbitTemplate.convertAndSend(
				RabbitMqTopology.DOMAIN_EVENTS_EXCHANGE,
				RabbitMqTopology.STATE_CAPACITY_THRESHOLD_ROUTING_KEY,
				event
		);
	}
}
