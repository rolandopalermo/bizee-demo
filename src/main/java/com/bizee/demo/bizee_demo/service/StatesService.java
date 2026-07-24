package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.dto.StateResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Source of truth for US state codes used by REST and Thymeleaf forms.
 */
@Service
public class StatesService {

	private static final List<StateResponse> US_STATES = List.of(
			new StateResponse("AL", "Alabama"),
			new StateResponse("AK", "Alaska"),
			new StateResponse("AZ", "Arizona"),
			new StateResponse("AR", "Arkansas"),
			new StateResponse("CA", "California"),
			new StateResponse("CO", "Colorado"),
			new StateResponse("CT", "Connecticut"),
			new StateResponse("DE", "Delaware"),
			new StateResponse("DC", "District of Columbia"),
			new StateResponse("FL", "Florida"),
			new StateResponse("GA", "Georgia"),
			new StateResponse("HI", "Hawaii"),
			new StateResponse("ID", "Idaho"),
			new StateResponse("IL", "Illinois"),
			new StateResponse("IN", "Indiana"),
			new StateResponse("IA", "Iowa"),
			new StateResponse("KS", "Kansas"),
			new StateResponse("KY", "Kentucky"),
			new StateResponse("LA", "Louisiana"),
			new StateResponse("ME", "Maine"),
			new StateResponse("MD", "Maryland"),
			new StateResponse("MA", "Massachusetts"),
			new StateResponse("MI", "Michigan"),
			new StateResponse("MN", "Minnesota"),
			new StateResponse("MS", "Mississippi"),
			new StateResponse("MO", "Missouri"),
			new StateResponse("MT", "Montana"),
			new StateResponse("NE", "Nebraska"),
			new StateResponse("NV", "Nevada"),
			new StateResponse("NH", "New Hampshire"),
			new StateResponse("NJ", "New Jersey"),
			new StateResponse("NM", "New Mexico"),
			new StateResponse("NY", "New York"),
			new StateResponse("NC", "North Carolina"),
			new StateResponse("ND", "North Dakota"),
			new StateResponse("OH", "Ohio"),
			new StateResponse("OK", "Oklahoma"),
			new StateResponse("OR", "Oregon"),
			new StateResponse("PA", "Pennsylvania"),
			new StateResponse("RI", "Rhode Island"),
			new StateResponse("SC", "South Carolina"),
			new StateResponse("SD", "South Dakota"),
			new StateResponse("TN", "Tennessee"),
			new StateResponse("TX", "Texas"),
			new StateResponse("UT", "Utah"),
			new StateResponse("VT", "Vermont"),
			new StateResponse("VA", "Virginia"),
			new StateResponse("WA", "Washington"),
			new StateResponse("WV", "West Virginia"),
			new StateResponse("WI", "Wisconsin"),
			new StateResponse("WY", "Wyoming")
	);

	public List<StateResponse> listStates() {
		return US_STATES;
	}
}
