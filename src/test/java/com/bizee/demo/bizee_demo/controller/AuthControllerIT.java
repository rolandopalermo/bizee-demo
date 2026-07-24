package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.auth.SessionAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Session-based auth for Thymeleaf UI against the users table (no Spring Security).
 */
class AuthControllerIT extends AbstractControllerIT {

	private static final String SEED_EMAIL = "alice.johnson@example.com";
	private static final String SEED_PASSWORD = "alice.johnson@example.com";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void protectedHome_withoutSession_redirectsToLogin() throws Exception {
		mockMvc.perform(get("/home"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void login_withInvalidCredentials_staysOnLogin() throws Exception {
		mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("email", SEED_EMAIL)
						.param("password", "wrong"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(containsString("Invalid email or password")))
				.andExpect(request().sessionAttributeDoesNotExist(SessionAuth.USER_ID_ATTRIBUTE));
	}

	@Test
	void login_withValidCredentials_thenHomeIsAccessible() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("email", SEED_EMAIL)
						.param("password", SEED_PASSWORD))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/home"))
				.andExpect(request().sessionAttribute(SessionAuth.USER_ID_ATTRIBUTE, 1L))
				.andExpect(request().sessionAttribute(SessionAuth.DISPLAY_NAME_ATTRIBUTE, "Alice Johnson"))
				.andExpect(request().sessionAttribute(SessionAuth.EMAIL_ATTRIBUTE, SEED_EMAIL))
				.andReturn();

		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/home").session(session))
				.andExpect(status().isOk())
				.andExpect(view().name("home"))
				.andExpect(content().string(containsString("Alice Johnson")))
				.andExpect(content().string(containsString(SEED_EMAIL)));
	}

	@Test
	void logout_invalidatesSession_andHomeRedirectsToLogin() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("email", SEED_EMAIL)
						.param("password", SEED_PASSWORD))
				.andExpect(status().isFound())
				.andReturn();

		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(post("/logout").session(session))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login"));

		mockMvc.perform(get("/home").session(session))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void api_withoutSession_isNotRedirectedToLogin() throws Exception {
		mockMvc.perform(post("/api/companies")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Auth IT Co",
								  "state": "CA",
								  "useRegisteredAgentService": false
								}
								"""))
				.andExpect(status().isCreated());
	}
}
