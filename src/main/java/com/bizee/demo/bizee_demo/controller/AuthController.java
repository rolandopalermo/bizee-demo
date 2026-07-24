package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.auth.SessionAuth;
import com.bizee.demo.bizee_demo.dto.LoginForm;
import com.bizee.demo.bizee_demo.entity.User;
import com.bizee.demo.bizee_demo.service.UserAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

	private final UserAuthService userAuthService;

	public AuthController(UserAuthService userAuthService) {
		this.userAuthService = userAuthService;
	}

	@GetMapping("/login")
	public String loginForm(HttpSession session, Model model) {
		if (SessionAuth.isAuthenticated(session)) {
			return "redirect:/home";
		}
		if (!model.containsAttribute("loginForm")) {
			model.addAttribute("loginForm", new LoginForm());
		}
		return "login";
	}

	@PostMapping("/login")
	public String login(
			@Valid @ModelAttribute("loginForm") LoginForm loginForm,
			BindingResult bindingResult,
			HttpSession session,
			Model model
	) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("error", "Email and password are required.");
			return "login";
		}

		return userAuthService.authenticate(loginForm.getEmail(), loginForm.getPassword())
				.map(user -> {
					loginSuccess(session, user);
					return "redirect:/home";
				})
				.orElseGet(() -> {
					model.addAttribute("error", "Invalid email or password.");
					loginForm.setPassword(null);
					return "login";
				});
	}

	@PostMapping("/logout")
	public String logout(HttpSession session) {
		SessionAuth.clear(session);
		return "redirect:/login";
	}

	private void loginSuccess(HttpSession session, User user) {
		SessionAuth.setAuthenticated(session, user.getId(), user.getName(), user.getEmail());
	}
}
