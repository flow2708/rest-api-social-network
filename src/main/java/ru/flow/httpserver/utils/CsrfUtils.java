package ru.flow.httpserver.utils;

import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class CsrfUtils {
    public static final String generateCSRF(HttpSession session) {
        String token = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", token);
        return token;
    }
}
