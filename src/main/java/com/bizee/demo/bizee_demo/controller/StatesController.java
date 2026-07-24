package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.StateResponse;
import com.bizee.demo.bizee_demo.service.StatesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public read endpoint for US state dropdowns. No {@code X-User-Id} required.
 */
@RestController
@RequestMapping("/api/states")
public class StatesController {

	private final StatesService statesService;

	public StatesController(StatesService statesService) {
		this.statesService = statesService;
	}

	@GetMapping
	public List<StateResponse> listStates() {
		return statesService.listStates();
	}
}
