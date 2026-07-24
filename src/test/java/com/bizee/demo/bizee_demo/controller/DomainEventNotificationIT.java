package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies RabbitMQ publish/consume notification side effects (console "mail" logs).
 * Intentionally not {@code @Transactional} so after-commit event publishing runs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class DomainEventNotificationIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createCompany_withService_logsAgentAssignmentNotification(CapturedOutput output) throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Event Notify Co",
								  "state": "NY",
								  "useRegisteredAgentService": true
								}
								"""))
				.andExpect(status().isCreated());

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
				assertThat(output.getOut())
						.contains("MAIL SENT (simulated)")
						.contains("New company assignment")
						.contains("Event Notify Co")
						.contains("NY")
		);
	}
}
