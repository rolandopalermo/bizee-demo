package com.bizee.demo.bizee_demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller ITs against PostgreSQL via Testcontainers ({@link AbstractControllerIT}).
 */
class RegisteredAgentControllerIT extends AbstractControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void checkCapacity_forCalifornia_returnsAvailable() throws Exception {
		mockMvc.perform(get("/api/registered-agents/capacity")
						.param("state", "CA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("CA")))
				.andExpect(jsonPath("$.available", is(true)))
				.andExpect(jsonPath("$.remainingCapacity", greaterThan(0)));
	}

	@Test
	void checkCapacity_normalizesLowercaseState() throws Exception {
		mockMvc.perform(get("/api/registered-agents/capacity")
						.param("state", "tx"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("TX")))
				.andExpect(jsonPath("$.available", is(true)))
				.andExpect(jsonPath("$.remainingCapacity", greaterThan(0)));
	}

	@Test
	void checkCapacity_forIllinois_returnsUnavailable() throws Exception {
		mockMvc.perform(get("/api/registered-agents/capacity")
						.param("state", "IL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("IL")))
				.andExpect(jsonPath("$.available", is(false)))
				.andExpect(jsonPath("$.remainingCapacity", is(0)));
	}

	@Test
	void checkCapacity_invalidState_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/registered-agents/capacity")
						.param("state", "California"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void listAgents_returnsSeededAgents() throws Exception {
		mockMvc.perform(get("/api/registered-agents"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(51)));
	}

	@Test
	void getAgent_returnsSeededAgent() throws Exception {
		mockMvc.perform(get("/api/registered-agents/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.state", notNullValue()))
				.andExpect(jsonPath("$.capacity", greaterThan(0)));
	}

	@Test
	void createUpdateDeleteAgent_roundTrip() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/registered-agents")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "state": "IL",
								  "name": "Illinois Registered Agent",
								  "email": "agent.il.it@example.com",
								  "capacity": 8
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.state", is("IL")))
				.andExpect(jsonPath("$.capacity", is(8)))
				.andReturn();

		Number agentId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(put("/api/registered-agents/{id}", agentId.longValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "state": "IL",
								  "name": "Illinois Registered Agent Updated",
								  "email": "agent.il.it@example.com",
								  "capacity": 10
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Illinois Registered Agent Updated")))
				.andExpect(jsonPath("$.capacity", is(10)));

		mockMvc.perform(delete("/api/registered-agents/{id}", agentId.longValue()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/registered-agents/{id}", agentId.longValue()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteAgent_whenAssigned_returnsBadRequest() throws Exception {
		// Seed company 2 uses CA registered agent service.
		mockMvc.perform(get("/api/companies/{id}", 2L)
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.registeredAgentType", is("registered_agent")));

		Number agentId = JsonPath.read(
				mockMvc.perform(get("/api/companies/{id}", 2L).header("X-User-Id", "1"))
						.andReturn()
						.getResponse()
						.getContentAsString(),
				"$.registeredAgentId"
		);

		mockMvc.perform(delete("/api/registered-agents/{id}", agentId.longValue()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAgent_invalidCapacity_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/registered-agents")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "state": "IL",
								  "name": "Bad Capacity Agent",
								  "email": "bad.capacity@example.com",
								  "capacity": 3
								}
								"""))
				.andExpect(status().isBadRequest());
	}
}
