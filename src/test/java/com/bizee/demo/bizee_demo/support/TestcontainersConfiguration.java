package com.bizee.demo.bizee_demo.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Shared Testcontainers for Spring Boot integration tests.
 * {@link ServiceConnection} supplies JDBC and AMQP connection details so Flyway, JPA,
 * and RabbitMQ target the containers instead of {@code application.properties} localhost.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		// Non-management image starts faster under constrained Docker Desktop resources.
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-alpine"))
				.withStartupTimeout(Duration.ofMinutes(3));
	}
}
