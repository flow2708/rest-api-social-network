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
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (session != null) {
            String csrfToken = CsrfUtils.generateCSRF(session);
            resp.sendRedirect(req.getContextPath() + "/mainpage?csrfToken=" + csrfToken);
            return;
        }

        PrintWriter out = resp.getWriter();
        //String csrfToken = CsrfUtils.generateCSRF(req.getSession());
        //req.setAttribute("csrfToken", csrfToken);

        resp.setContentType("text/html;charset=UTF-8");
        //req.getRequestDispatcher("login.html").forward(req, resp);
        out.println("<form action='login' method='POST' xmlns:th='http://www.w3.org/1999/xhtml'>");
        //out.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
        out.println("<input type='text' name='username' placeholder='Логин' required>");
        out.println("<input type='password' name='password' placeholder='Пароль' required>");
        out.println("<button type='submit'>Войти</button>");
        out.println("<a href='register'>Регистрация</a>");
        out.println("</form>");
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }*/

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            MySQL db = new MySQL();
            User user = db.findByUsername(username);

            HttpSession session = req.getSession();

            if (user == null) {
                resp.sendRedirect("login?error=not_found");
                return;
            }

            try {
                PasswordUtils.validate(password);
            } catch (IllegalArgumentException e) {
                resp.sendRedirect("login?error=incorrect_password_format");
                return;
            }

            if (!PasswordUtils.checkPassword(password, user.getPassword())) {
                resp.sendRedirect("login?error=wrong_pass");
                return;
            }

            session.setAttribute("user", user);
            String csrfToken = CsrfUtils.generateCSRF(session);
            resp.sendRedirect(req.getContextPath() + "/mainpage?csrfToken=" + csrfToken);

        } catch (Exception e) {
            resp.sendRedirect("error500.html");
        }
    }
}