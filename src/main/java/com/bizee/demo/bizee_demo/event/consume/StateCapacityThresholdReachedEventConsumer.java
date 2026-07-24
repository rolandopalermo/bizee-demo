package com.bizee.demo.bizee_demo.event.consume;

import com.bizee.demo.bizee_demo.event.RabbitMqTopology;
import com.bizee.demo.bizee_demo.event.dto.StateCapacityThresholdReachedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Handles side effects for {@link StateCapacityThresholdReachedEvent}.
 * Does not send real email — logs a simulated notification to the console.
 */
@Service
public class StateCapacityThresholdReachedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(StateCapacityThresholdReachedEventConsumer.class);

	@RabbitListener(queues = RabbitMqTopology.STATE_CAPACITY_THRESHOLD_QUEUE)
	public void onStateCapacityThresholdReached(StateCapacityThresholdReachedEvent event) {
		log.info(
				"MAIL SENT (simulated): To={} | Subject=Registered-agent capacity threshold | "
						+ "State={} utilization={}% (used={}/total={})",
				event.adminEmail(),
				event.state(),
				String.format("%.1f", event.utilizationPercent()),
				event.usedCapacity(),
				event.totalCapacity()
		);
	}
}
