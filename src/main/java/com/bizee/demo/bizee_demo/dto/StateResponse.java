package com.bizee.demo.bizee_demo.dto;

/**
 * US state (or district) option for dropdowns and APIs.
 */
public record StateResponse(
		String code,
		String name
) {
}
