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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        //String csrfToken = CsrfUtils.generateCSRF(req.getSession());
        //req.setAttribute("csrfToken", csrfToken);

        resp.setContentType("text/html;charset=UTF-8");
        //req.getRequestDispatcher("register.html").forward(req, resp);
        out.println("<form action='register' method='POST' xmlns:th='http://www.w3.org/1999/xhtml'>");
        //out.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
        out.println("<input type='text' name='username' placeholder='Логин' required>");
        out.println("<input type='email' name='email' placeholder='Email' required>");
        out.println("<input type='password' name='password' placeholder='Пароль' required>");
        out.println("<button type='submit'>Зарегистрироваться</button>");
        out.println("<a href='login'>Уже есть аккаунт?</a>");
        out.println("</form>");
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }*/

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String ipAddress = req.getHeader("X-FORWARDED-FOR");

        try {
            MySQL db = new MySQL();
            HttpSession session = req.getSession();

            if (db.findByUsername(username) != null) {
                resp.sendRedirect("register?error=exists");
                return;
            }

            try {
                PasswordUtils.validate(password);
            } catch (IllegalArgumentException e) {
                resp.sendRedirect("login?error=incorrect_password_format");
                return;
            }

            if (ipAddress == null) {
                ipAddress = req.getRemoteAddr();
            }

            if (!db.saveUser(username, email, password, 0, ipAddress)) {
                resp.sendRedirect("register?error=save_failed");
                return;
            }

            session.setAttribute("user", new User(username, email, password, 0));
            String csrfToken = CsrfUtils.generateCSRF(session);
            resp.sendRedirect(req.getContextPath() + "/mainpage?csrfToken=" + csrfToken);

        } catch (Exception e) {
            resp.sendRedirect("error500.html");
        }
    }
}