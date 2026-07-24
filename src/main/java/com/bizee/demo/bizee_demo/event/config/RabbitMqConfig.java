package com.bizee.demo.bizee_demo.event.config;

import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	@Bean
	TopicExchange domainEventsExchange() {
		return new TopicExchange(RabbitMqTopology.DOMAIN_EVENTS_EXCHANGE, true, false);
	}

	@Bean
	Queue registeredAgentAssignedQueue() {
		return new Queue(RabbitMqTopology.REGISTERED_AGENT_ASSIGNED_QUEUE, true);
	}

	@Bean
	Queue stateCapacityThresholdQueue() {
		return new Queue(RabbitMqTopology.STATE_CAPACITY_THRESHOLD_QUEUE, true);
	}

	@Bean
	Binding registeredAgentAssignedBinding(
			Queue registeredAgentAssignedQueue,
			TopicExchange domainEventsExchange
	) {
		return BindingBuilder
				.bind(registeredAgentAssignedQueue)
				.to(domainEventsExchange)
				.with(RabbitMqTopology.REGISTERED_AGENT_ASSIGNED_ROUTING_KEY);
	}

	@Bean
	Binding stateCapacityThresholdBinding(
			Queue stateCapacityThresholdQueue,
			TopicExchange domainEventsExchange
	) {
		return BindingBuilder
				.bind(stateCapacityThresholdQueue)
				.to(domainEventsExchange)
				.with(RabbitMqTopology.STATE_CAPACITY_THRESHOLD_ROUTING_KEY);
	}

	@Bean
	MessageConverter rabbitMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
