package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.support.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base for controller integration tests: full Spring context + MockMvc against
 * PostgreSQL and RabbitMQ Testcontainers. Flyway migrations (including seed data) run on startup.
 * {@link Transactional} rolls back each test so seed rows are not permanently changed.
 * Note: domain events published via after-commit hooks will not fire under test rollback;
 * see {@link DomainEventNotificationIT} for messaging coverage.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
abstract class AbstractControllerIT {
}
