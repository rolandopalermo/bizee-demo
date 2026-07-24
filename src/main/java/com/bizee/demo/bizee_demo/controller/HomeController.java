package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.auth.SessionAuth;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping({"/", "/home"})
	public String home(HttpSession session, Model model) {
		model.addAttribute("displayName", SessionAuth.getDisplayName(session));
		model.addAttribute("email", SessionAuth.getEmail(session));
		return "home";
	}
}
