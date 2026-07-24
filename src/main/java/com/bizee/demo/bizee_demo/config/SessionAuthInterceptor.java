package com.bizee.demo.bizee_demo.config;

import com.bizee.demo.bizee_demo.auth.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protects browser (Thymeleaf) routes. REST {@code /api/**} is excluded via WebMvcConfig.
 */
public class SessionAuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession(false);
		if (SessionAuth.isAuthenticated(session)) {
			return true;
		}
		response.sendRedirect(request.getContextPath() + "/login");
		return false;
	}
}
