package com.bizee.demo.bizee_demo.auth;

import jakarta.servlet.http.HttpSession;

/**
 * Session keys and helpers for browser auth against the {@code users} table (no Spring Security).
 */
public final class SessionAuth {

	public static final String USER_ID_ATTRIBUTE = "AUTHENTICATED_USER_ID";
	public static final String DISPLAY_NAME_ATTRIBUTE = "AUTHENTICATED_DISPLAY_NAME";
	public static final String EMAIL_ATTRIBUTE = "AUTHENTICATED_EMAIL";

	private SessionAuth() {
	}

	public static boolean isAuthenticated(HttpSession session) {
		return session != null && session.getAttribute(USER_ID_ATTRIBUTE) != null;
	}

	public static Long getUserId(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object value = session.getAttribute(USER_ID_ATTRIBUTE);
		return value instanceof Long userId ? userId : null;
	}

	public static String getDisplayName(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object value = session.getAttribute(DISPLAY_NAME_ATTRIBUTE);
		return value instanceof String name ? name : null;
	}

	public static String getEmail(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object value = session.getAttribute(EMAIL_ATTRIBUTE);
		return value instanceof String email ? email : null;
	}

	public static void setAuthenticated(HttpSession session, Long userId, String displayName, String email) {
		session.setAttribute(USER_ID_ATTRIBUTE, userId);
		session.setAttribute(DISPLAY_NAME_ATTRIBUTE, displayName);
		session.setAttribute(EMAIL_ATTRIBUTE, email);
	}

	public static void clear(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
	}
}
