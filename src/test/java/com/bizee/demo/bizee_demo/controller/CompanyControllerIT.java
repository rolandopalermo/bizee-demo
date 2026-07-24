package com.bizee.demo.bizee_demo.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller ITs against PostgreSQL via Testcontainers ({@link AbstractControllerIT}).
 */
class CompanyControllerIT extends AbstractControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createCompany_asSelf_returnsCreated() throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "IT Self RA Co",
								  "state": "NY",
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.userId", is(1)))
				.andExpect(jsonPath("$.name", is("IT Self RA Co")))
				.andExpect(jsonPath("$.state", is("NY")))
				.andExpect(jsonPath("$.registeredAgentType", is("user")))
				.andExpect(jsonPath("$.registeredAgentId", is(1)));
	}

	@Test
	void createCompany_withService_assignsRegisteredAgent() throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "IT Service RA Co",
								  "state": "CA",
								  "useRegisteredAgentService": true
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.state", is("CA")))
				.andExpect(jsonPath("$.registeredAgentType", is("registered_agent")))
				.andExpect(jsonPath("$.registeredAgentId", greaterThan(0)));
	}

	@Test
	void createCompany_unknownUser_returnsNotFound() throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "99999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Ghost Co",
								  "state": "CA",
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void createCompany_noCapacityInIllinois_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "No Capacity Co",
								  "state": "IL",
								  "useRegisteredAgentService": true
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateRegisteredAgent_toSelf_returnsOk() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "2")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "IT Update Target Co",
								  "state": "TX",
								  "useRegisteredAgentService": true
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		Number companyId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(put("/api/companies/{id}/registered-agent", companyId.longValue())
						.header("X-User-Id", "2")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(companyId.intValue())))
				.andExpect(jsonPath("$.registeredAgentType", is("user")))
				.andExpect(jsonPath("$.registeredAgentId", is(2)));
	}

	@Test
	void updateRegisteredAgent_otherUsersCompany_returnsNotFound() throws Exception {
		// Seed company id=1 belongs to user 1; user 5 must not update it.
		mockMvc.perform(put("/api/companies/{id}/registered-agent", 1L)
						.header("X-User-Id", "5")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void listCompanies_returnsOwnedCompaniesOnly() throws Exception {
		mockMvc.perform(get("/api/companies")
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].userId", is(1)))
				.andExpect(jsonPath("$[1].userId", is(1)));
	}

	@Test
	void getCompany_owned_returnsOk() throws Exception {
		mockMvc.perform(get("/api/companies/{id}", 1L)
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("Alice Consulting LLC")));
	}

	@Test
	void getCompany_otherUsersCompany_returnsNotFound() throws Exception {
		mockMvc.perform(get("/api/companies/{id}", 1L)
						.header("X-User-Id", "5"))
				.andExpect(status().isNotFound());
	}

	@Test
	void updateCompany_owned_returnsOk() throws Exception {
		mockMvc.perform(put("/api/companies/{id}", 1L)
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Alice Consulting Renamed LLC",
								  "state": "CA",
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name", is("Alice Consulting Renamed LLC")))
				.andExpect(jsonPath("$.registeredAgentType", is("user")))
				.andExpect(jsonPath("$.registeredAgentId", is(1)));
	}

	@Test
	void deleteCompany_owned_returnsNoContent() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Disposable Co",
								  "state": "NY",
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		Number companyId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(delete("/api/companies/{id}", companyId.longValue())
						.header("X-User-Id", "1"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/companies/{id}", companyId.longValue())
						.header("X-User-Id", "1"))
				.andExpect(status().isNotFound());
	}
}
