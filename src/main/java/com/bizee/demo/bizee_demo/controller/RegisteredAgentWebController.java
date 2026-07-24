package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.CreateRegisteredAgentRequest;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentForm;
import com.bizee.demo.bizee_demo.dto.RegisteredAgentResponse;
import com.bizee.demo.bizee_demo.dto.UpdateRegisteredAgentEntityRequest;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.service.RegisteredAgentService;
import com.bizee.demo.bizee_demo.service.StatesService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registered-agents")
public class RegisteredAgentWebController {

	private final RegisteredAgentService registeredAgentService;
	private final StatesService statesService;

	public RegisteredAgentWebController(
			RegisteredAgentService registeredAgentService,
			StatesService statesService
	) {
		this.registeredAgentService = registeredAgentService;
		this.statesService = statesService;
	}

	@GetMapping
	public String list(@RequestParam(required = false) String state, Model model) {
		String filterState = (state == null || state.isBlank()) ? null : state.trim().toUpperCase();
		model.addAttribute("filterState", filterState);
		model.addAttribute("states", statesService.listStates());
		model.addAttribute("agents", registeredAgentService.listAgents(filterState));
		return "registered-agents/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("agentForm", new RegisteredAgentForm());
		model.addAttribute("editing", false);
		return "registered-agents/form";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute("agentForm") RegisteredAgentForm agentForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("editing", false);
			return "registered-agents/form";
		}
		try {
			registeredAgentService.createAgent(new CreateRegisteredAgentRequest(
					agentForm.getState(),
					agentForm.getName(),
					agentForm.getEmail(),
					agentForm.getCapacity()
			));
			redirectAttributes.addFlashAttribute("message", "Registered agent created.");
			return "redirect:/registered-agents";
		}
		catch (BusinessException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("editing", false);
			return "registered-agents/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(
			@PathVariable Long id,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			RegisteredAgentResponse agent = registeredAgentService.getAgent(id);
			RegisteredAgentForm form = new RegisteredAgentForm();
			form.setState(agent.state());
			form.setName(agent.name());
			form.setEmail(agent.email());
			form.setCapacity(agent.capacity());
			model.addAttribute("agentForm", form);
			model.addAttribute("agentId", id);
			model.addAttribute("editing", true);
			return "registered-agents/form";
		}
		catch (ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/registered-agents";
		}
	}

	@PostMapping("/{id}")
	public String update(
			@PathVariable Long id,
			@Valid @ModelAttribute("agentForm") RegisteredAgentForm agentForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("agentId", id);
			model.addAttribute("editing", true);
			return "registered-agents/form";
		}
		try {
			registeredAgentService.updateAgent(id, new UpdateRegisteredAgentEntityRequest(
					agentForm.getState(),
					agentForm.getName(),
					agentForm.getEmail(),
					agentForm.getCapacity()
			));
			redirectAttributes.addFlashAttribute("message", "Registered agent updated.");
			return "redirect:/registered-agents";
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("agentId", id);
			model.addAttribute("editing", true);
			return "registered-agents/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			registeredAgentService.deleteAgent(id);
			redirectAttributes.addFlashAttribute("message", "Registered agent deleted.");
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/registered-agents";
	}
}
