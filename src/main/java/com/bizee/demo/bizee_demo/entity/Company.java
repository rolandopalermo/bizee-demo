package com.bizee.demo.bizee_demo.entity;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "companies")
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false, length = 200)
	private String name;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, length = 2)
	private String state;

	@Column(name = "registered_agent_type", nullable = false, length = 30)
	private RegisteredAgentType registeredAgentType;

	@Column(name = "registered_agent_id", nullable = false)
	private Long registeredAgentId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public RegisteredAgentType getRegisteredAgentType() {
		return registeredAgentType;
	}

	public void setRegisteredAgentType(RegisteredAgentType registeredAgentType) {
		this.registeredAgentType = registeredAgentType;
	}

	public Long getRegisteredAgentId() {
		return registeredAgentId;
	}

	public void setRegisteredAgentId(Long registeredAgentId) {
		this.registeredAgentId = registeredAgentId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
