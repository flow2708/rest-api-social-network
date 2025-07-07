package ru.flow.httpserver.controller.authorization;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.utils.CsrfUtils;
import ru.flow.httpserver.utils.PasswordUtils;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;

import java.io.IOException;
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String ipAddress = req.getHeader("X-FORWARDED-FOR");
        
        try {
            MySQL db = new MySQL();
            HttpSession session = req.getSession();

            if (db.findByUsername(username) != null) {
                resp.sendRedirect("register.html?error=exists");
                return;
            }

            try {
                PasswordUtils.validate(password);
            } catch (IllegalArgumentException e) {
                resp.sendRedirect("login.html?error=incorrect_password_format");
                return;
            }

            if (ipAddress == null) {
                ipAddress = req.getRemoteAddr();
            }

            if (!db.saveUser(username, email, password, 0, ipAddress)) {
                resp.sendRedirect("register.html?error=save_failed");
                return;
            }

            session.setAttribute("user", new User(username, email, password, 0));
            CsrfUtils.generateCSRF(session);
            resp.sendRedirect("mainpage.html");

        } catch (Exception e) {
            resp.sendRedirect("error500.html");
        }
    }
}