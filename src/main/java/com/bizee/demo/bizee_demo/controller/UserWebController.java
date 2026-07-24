package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.CreateUserRequest;
import com.bizee.demo.bizee_demo.dto.UpdateUserRequest;
import com.bizee.demo.bizee_demo.dto.UserForm;
import com.bizee.demo.bizee_demo.dto.UserResponse;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserWebController {

	private final UserService userService;

	public UserWebController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public String list(Model model) {
		model.addAttribute("users", userService.listUsers());
		return "users/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("userForm", new UserForm());
		model.addAttribute("editing", false);
		return "users/form";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute("userForm") UserForm userForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		if (userForm.getPassword() == null || userForm.getPassword().isBlank()) {
			bindingResult.rejectValue("password", "required", "password is required");
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("editing", false);
			return "users/form";
		}
		try {
			userService.createUser(new CreateUserRequest(
					userForm.getName(),
					userForm.getEmail(),
					userForm.getPassword()
			));
			redirectAttributes.addFlashAttribute("message", "User created.");
			return "redirect:/users";
		}
		catch (BusinessException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("editing", false);
			return "users/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String editForm(
			@PathVariable Long id,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		try {
			UserResponse user = userService.getUser(id);
			UserForm form = new UserForm();
			form.setName(user.name());
			form.setEmail(user.email());
			model.addAttribute("userForm", form);
			model.addAttribute("userId", id);
			model.addAttribute("editing", true);
			return "users/form";
		}
		catch (ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
			return "redirect:/users";
		}
	}

	@PostMapping("/{id}")
	public String update(
			@PathVariable Long id,
			@Valid @ModelAttribute("userForm") UserForm userForm,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes
	) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("userId", id);
			model.addAttribute("editing", true);
			return "users/form";
		}
		try {
			userService.updateUser(id, new UpdateUserRequest(
					userForm.getName(),
					userForm.getEmail(),
					userForm.getPassword()
			));
			redirectAttributes.addFlashAttribute("message", "User updated.");
			return "redirect:/users";
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			model.addAttribute("error", ex.getMessage());
			model.addAttribute("userId", id);
			model.addAttribute("editing", true);
			return "users/form";
		}
	}

	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			userService.deleteUser(id);
			redirectAttributes.addFlashAttribute("message", "User deleted.");
		}
		catch (BusinessException | ResourceNotFoundException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getMessage());
		}
		return "redirect:/users";
	}
}
