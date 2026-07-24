package com.bizee.demo.bizee_demo.controller;

import com.bizee.demo.bizee_demo.dto.CreateUserRequest;
import com.bizee.demo.bizee_demo.dto.UpdateUserRequest;
import com.bizee.demo.bizee_demo.dto.UserResponse;
import com.bizee.demo.bizee_demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> listUsers() {
		return userService.listUsers();
	}

	@GetMapping("/{id}")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getUser(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
		return userService.createUser(request);
	}

	@PutMapping("/{id}")
	public UserResponse updateUser(
			@PathVariable Long id,
			@Valid @RequestBody UpdateUserRequest request
	) {
		return userService.updateUser(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
	}
}
