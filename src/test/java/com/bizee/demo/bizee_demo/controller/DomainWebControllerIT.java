package com.bizee.demo.bizee_demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Thymeleaf CRUD pages require session auth against the users table.
 */
class DomainWebControllerIT extends AbstractControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void companiesList_withoutSession_redirectsToLogin() throws Exception {
		mockMvc.perform(get("/companies"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void companiesList_withSession_rendersAllCompaniesAndOwners() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/companies").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("companies/list"))
				.andExpect(content().string(containsString("Alice Consulting LLC")))
				.andExpect(content().string(containsString("Brian Logistics LLC")))
				.andExpect(content().string(containsString("Alice Johnson")))
				.andExpect(content().string(containsString("All users")));
	}

	@Test
	void companiesList_filterByUser_showsOnlyThatUsersCompanies() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/companies").param("userId", "1").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("companies/list"))
				.andExpect(content().string(containsString("Alice Consulting LLC")))
				.andExpect(content().string(not(containsString("Brian Logistics LLC"))));
	}

	@Test
	void registeredAgentsList_withSession_rendersAgents() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/registered-agents").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("registered-agents/list"))
				.andExpect(content().string(containsString("Registered agents")));
	}

	@Test
	void registeredAgentCreateForm_rendersStateDropdownWiredToApi() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/registered-agents/new").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("registered-agents/form"))
				.andExpect(content().string(containsString("<select")))
				.andExpect(content().string(containsString("name=\"state\"")))
				.andExpect(content().string(containsString("data-states-select")))
				.andExpect(content().string(containsString("/js/states-select.js")));
	}

	@Test
	void companyCreateForm_rendersStateDropdownWiredToApi() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/companies/new").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("companies/form"))
				.andExpect(content().string(containsString("<select")))
				.andExpect(content().string(containsString("name=\"state\"")))
				.andExpect(content().string(containsString("data-states-select")))
				.andExpect(content().string(containsString("/js/states-select.js")));
	}

	@Test
	void usersList_withSession_rendersUsers() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(get("/users").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("users/list"))
				.andExpect(content().string(containsString("Alice Johnson")));
	}

	@Test
	void createCompanyViaForm_persistsForSessionUser() throws Exception {
		MockHttpSession session = login();

		mockMvc.perform(post("/companies")
						.session(session)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("name", "Web Form Co")
						.param("state", "NY")
						.param("useRegisteredAgentService", "false"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/companies"));

		mockMvc.perform(get("/companies").param("userId", "1").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Web Form Co")));
	}

	private MockHttpSession login() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("email", "alice.johnson@example.com")
						.param("password", "alice.johnson@example.com"))
				.andExpect(status().isFound())
				.andReturn();
		return (MockHttpSession) loginResult.getRequest().getSession(false);
	}
}
