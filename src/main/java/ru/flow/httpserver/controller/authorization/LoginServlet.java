package ru.flow.httpserver.controller.authorization;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.flow.httpserver.utils.PasswordUtils;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            MySQL db = new MySQL();
            User user = db.findByUsername(username);

            if (user == null) {
                resp.sendRedirect("login.html?error=not_found");
                return;
            }

            try {
                PasswordUtils.validate(password);
            } catch (IllegalArgumentException e) {
                resp.sendRedirect("login.html?error=incorrect_password_format");
                return;
            }

            if (!PasswordUtils.checkPassword(password, user.getPassword())) {
                resp.sendRedirect("login.html?error=wrong_pass");
                return;
            }

            req.getSession().setAttribute("user", user);
            resp.sendRedirect("mainpage.html");

        } catch (Exception e) {
            resp.sendRedirect("error500.html");
        }
    }
}