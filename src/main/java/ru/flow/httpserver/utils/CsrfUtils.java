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
    public static final boolean isValidCSRF(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute("csrfToken");
        String requestToken = (String) request.getSession().getAttribute("csrfToken");

        return sessionToken != null && sessionToken.equals(requestToken);
    }
}
