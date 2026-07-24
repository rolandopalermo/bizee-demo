package com.bizee.demo.bizee_demo.service;

import com.bizee.demo.bizee_demo.dto.CreateUserRequest;
import com.bizee.demo.bizee_demo.dto.UpdateUserRequest;
import com.bizee.demo.bizee_demo.dto.UserResponse;
import com.bizee.demo.bizee_demo.entity.User;
import com.bizee.demo.bizee_demo.exception.BusinessException;
import com.bizee.demo.bizee_demo.exception.ResourceNotFoundException;
import com.bizee.demo.bizee_demo.repository.CompanyRepository;
import com.bizee.demo.bizee_demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(
			UserRepository userRepository,
			CompanyRepository companyRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> listUsers() {
		return userRepository.findAllByOrderByIdAsc().stream()
				.map(UserResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(Long id) {
		return UserResponse.from(requireUser(id));
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email, null);

		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));

		return UserResponse.from(userRepository.save(user));
	}

	@Transactional
	public UserResponse updateUser(Long id, UpdateUserRequest request) {
		User user = requireUser(id);
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email, id);

		user.setName(request.name().trim());
		user.setEmail(email);
		if (request.password() != null && !request.password().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.password()));
		}

		return UserResponse.from(userRepository.save(user));
	}

	@Transactional
	public void deleteUser(Long id) {
		User user = requireUser(id);
		if (companyRepository.existsByUserId(id)) {
			throw new BusinessException(
					"Cannot delete user id=" + id + "; user still owns one or more companies");
		}
		userRepository.delete(user);
	}

	private User requireUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found for id=" + id));
	}

	private void ensureEmailAvailable(String email, Long excludeId) {
		boolean taken = excludeId == null
				? userRepository.existsByEmailIgnoreCase(email)
				: userRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
		if (taken) {
			throw new BusinessException("User email already exists: " + email);
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.US);
	}
}
