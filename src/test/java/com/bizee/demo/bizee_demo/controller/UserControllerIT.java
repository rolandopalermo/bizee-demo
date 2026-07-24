package com.bizee.demo.bizee_demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
class UserControllerIT extends AbstractControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listUsers_returnsSeededUsers() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[0].email", notNullValue()))
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	void getUser_returnsSeededUser() throws Exception {
		mockMvc.perform(get("/api/users/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Alice Johnson")))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void createUpdateDeleteUser_roundTrip() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "IT User",
								  "email": "it.user@example.com",
								  "password": "secret"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("IT User")))
				.andExpect(jsonPath("$.email", is("it.user@example.com")))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();

		Number userId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(put("/api/users/{id}", userId.longValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "IT User Updated",
								  "email": "it.user@example.com",
								  "password": ""
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("IT User Updated")));

		mockMvc.perform(delete("/api/users/{id}", userId.longValue()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/{id}", userId.longValue()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteUser_withCompanies_returnsBadRequest() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", 1L))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createUser_duplicateEmail_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Dup",
								  "email": "alice.johnson@example.com",
								  "password": "x"
								}
								"""))
				.andExpect(status().isBadRequest());
	}
}
