package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.auth.SessionAuth;
import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.dto.CompanyForm;
import com.bizee.demo.bizee_demo.dto.CompanyResponse;
import com.bizee.demo.bizee_demo.dto.CreateCompanyRequest;
import com.bizee.demo.bizee_demo.dto.UpdateCompanyRequest;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.service.CompanyService;
import com.bizee.demo.bizee_demo.service.UserService;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/companies")
public class CompanyWebController {

	private final CompanyService companyService;
	private final UserService userService;

	public CompanyWebController(CompanyService companyService, UserService userService) {
		this.companyService = companyService;
		this.userService = userService;
	}

	@GetMapping
	public String list(
			@RequestParam(required = false) Long userId,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			model.addAttribute("filterUserId", userId);
			model.addAttribute("users", userService.listUsers());
			model.addAttribute("companies", companyService.listAllCompanies(userId));
			return "companies/list";
		}
		catch (ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/companies";
		}
	}

	@GetMapping("/new")
	public String createForm(HttpSession session, Model model) {
		model.addAttribute("companyForm", new CompanyForm());
		model.addAttribute("ownerDisplay", ownerDisplay(session));
		model.addAttribute("editing", false);
		return "companies/form";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute("companyForm") CompanyForm companyForm,
			BindingResult bindingResult,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		Long sessionUserId = requireSessionUserId(session);
		if (bindingResult.hasErrors()) {
			model.addAttribute("ownerDisplay", ownerDisplay(session));
			model.addAttribute("editing", false);
			return "companies/form";
		}
		try {
			companyService.createCompany(
					sessionUserId,
					new CreateCompanyRequest(
							companyForm.getName(),
							companyForm.getState(),
							companyForm.getUseRegisteredAgentService()
					)
			);
			redirectAttributes.addFlashAttribute("message", "Company created.");
			return "redirect:/companies";
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("ownerDisplay", ownerDisplay(session));
			model.addAttribute("editing", false);
			return "companies/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(
			@PathVariable Long id,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			CompanyResponse company = companyService.getCompanyById(id);
			CompanyForm form = new CompanyForm();
			form.setName(company.name());
			form.setState(company.state());
			form.setUseRegisteredAgentService(company.registeredAgentType() == RegisteredAgentType.REGISTERED_AGENT);
			model.addAttribute("companyForm", form);
			model.addAttribute("companyId", id);
			model.addAttribute("ownerDisplay", formatOwner(company));
			model.addAttribute("editing", true);
			return "companies/form";
		}
		catch (ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/companies";
		}
	}

	@PostMapping("/{id}")
	public String update(
			@PathVariable Long id,
			@Valid @ModelAttribute("companyForm") CompanyForm companyForm,
			BindingResult bindingResult,
			HttpSession session,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			CompanyResponse existing = companyService.getCompanyById(id);
			if (bindingResult.hasErrors()) {
				model.addAttribute("companyId", id);
				model.addAttribute("ownerDisplay", formatOwner(existing));
				model.addAttribute("editing", true);
				return "companies/form";
			}
			companyService.updateCompany(
					existing.userId(),
					id,
					new UpdateCompanyRequest(
							companyForm.getName(),
							companyForm.getState(),
							companyForm.getUseRegisteredAgentService()
					)
			);
			redirectAttributes.addFlashAttribute("message", "Company updated.");
			return "redirect:/companies";
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("companyId", id);
			model.addAttribute("ownerDisplay", ownerDisplay(session));
			model.addAttribute("editing", true);
			return "companies/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String delete(
			@PathVariable Long id,
			RedirectAttributes redirectAttributes
	) {
		try {
			CompanyResponse company = companyService.getCompanyById(id);
			companyService.deleteCompany(company.userId(), id);
			redirectAttributes.addFlashAttribute("message", "Company deleted.");
		}
		catch (ResourceNotFoundException | BusinessException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/companies";
	}

	private Long requireSessionUserId(HttpSession session) {
		Long userId = SessionAuth.getUserId(session);
		if (userId == null) {
			throw new ResourceNotFoundException("Not authenticated");
		}
		return userId;
	}

	private String ownerDisplay(HttpSession session) {
		String name = SessionAuth.getDisplayName(session);
		String email = SessionAuth.getEmail(session);
		if (name == null && email == null) {
			return "";
		}
		if (name == null) {
			return email;
		}
		if (email == null) {
			return name;
		}
		return name + " (" + email + ")";
	}

	private String formatOwner(CompanyResponse company) {
		String name = company.userName();
		String email = company.userEmail();
		if (name == null && email == null) {
			return "User #" + company.userId();
		}
		if (name == null) {
			return email;
		}
		if (email == null) {
			return name;
		}
		return name + " (" + email + ")";
	}
}
