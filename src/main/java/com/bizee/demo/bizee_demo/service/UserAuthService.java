package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.entity.User;
import com.bizee.demo.bizee_demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Authenticates browser logins against the {@code users} table using BCrypt password hashes.
 */
@Service
public class UserAuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public Optional<User> authenticate(String email, String password) {
		if (email == null || password == null) {
			return Optional.empty();
		}
		String normalizedEmail = email.trim();
		if (normalizedEmail.isEmpty()) {
			return Optional.empty();
		}
		return userRepository.findByEmailIgnoreCase(normalizedEmail)
				.filter(user -> passwordEncoder.matches(password, user.getPassword()));
	}
}
