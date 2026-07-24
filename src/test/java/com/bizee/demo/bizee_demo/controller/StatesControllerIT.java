package com.bizee.demo.bizee_demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Public states list for form dropdowns — no {@code X-User-Id} required.
 */
class StatesControllerIT extends AbstractControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listStates_withoutUserHeader_returnsUsStates() throws Exception {
		mockMvc.perform(get("/api/states"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(51)))
				.andExpect(jsonPath("$[0].code", is("AL")))
				.andExpect(jsonPath("$[0].name", is("Alabama")))
				.andExpect(jsonPath("$[4].code", is("CA")))
				.andExpect(jsonPath("$[4].name", is("California")))
				.andExpect(jsonPath("$[50].code", is("WY")))
				.andExpect(jsonPath("$[50].name", is("Wyoming")));
	}
}
