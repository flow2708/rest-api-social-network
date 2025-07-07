package ru.flow.httpserver.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class CsrfUtils {
    public static final String generateCSRF(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        return token;
    }
    public static final boolean isValid(HttpServletRequest request) {
        String sessionToken = (String) request.getSession().getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");
        return sessionToken != null && sessionToken.equals(requestToken);
    }
}
