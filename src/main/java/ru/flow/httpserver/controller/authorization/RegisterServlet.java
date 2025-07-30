package ru.flow.httpserver.controller.authorization;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.utils.CsrfUtils;
import ru.flow.httpserver.utils.HtmlUtils;
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
        resp.setContentType("text/html;charset=UTF-8");

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <title>Регистрация</title>");
        out.println("    <style>");
        out.println("        body {");
        out.println("            margin: 0;");
        out.println("            padding: 0;");
        out.println("            display: flex;");
        out.println("            justify-content: center;");
        out.println("            align-items: center;");
        out.println("            min-height: 100vh;");
        out.println("            font-family: 'Jost', sans-serif;");
        out.println("            background: linear-gradient(to bottom, #0f0c29, #302b63, #24243e);");
        out.println("        }");
        out.println("        .main {");
        out.println("            width: 350px;");
        out.println("            height: 500px;");
        out.println("            overflow: hidden;");
        out.println("            background: url(\"https://doc-08-2c-docs.googleusercontent.com/docs/securesc/68c90smiglihng9534mvqmq1946dmis5/fo0picsp1nhiucmc0l25s29respgpr4j/1631524275000/03522360960922298374/03522360960922298374/1Sx0jhdpEpnNIydS4rnN4kHSJtU1EyWka?e=view&authuser=0&nonce=gcrocepgbb17m&user=03522360960922298374&hash=tfhgbs86ka6divo3llbvp93mg4csvb38\") no-repeat center/ cover;");
        out.println("            border-radius: 10px;");
        out.println("            box-shadow: 5px 20px 50px #000;");
        out.println("        }");
        out.println("        .signup-form {");
        out.println("            height: 100%;");
        out.println("            display: flex;");
        out.println("            flex-direction: column;");
        out.println("            justify-content: center;");
        out.println("            align-items: center;");
        out.println("            background: rgba(255, 255, 255, 0.1);");
        out.println("            backdrop-filter: blur(5px);");
        out.println("        }");
        out.println("        .signup-form label {");
        out.println("            color: #fff;");
        out.println("            font-size: 2.3em;");
        out.println("            margin-bottom: 20px;");
        out.println("            font-weight: bold;");
        out.println("        }");
        out.println("        .signup-form input {");
        out.println("            width: 60%;");
        out.println("            height: 10px;");
        out.println("            background: #e0dede;");
        out.println("            margin: 12px auto;");
        out.println("            padding: 12px;");
        out.println("            border: none;");
        out.println("            outline: none;");
        out.println("            border-radius: 5px;");
        out.println("        }");
        out.println("        .signup-form button {");
        out.println("            width: 60%;");
        out.println("            height: 40px;");
        out.println("            margin: 15px auto;");
        out.println("            color: #fff;");
        out.println("            background: #573b8a;");
        out.println("            font-size: 1em;");
        out.println("            font-weight: bold;");
        out.println("            outline: none;");
        out.println("            border: none;");
        out.println("            border-radius: 5px;");
        out.println("            transition: .2s ease-in;");
        out.println("            cursor: pointer;");
        out.println("        }");
        out.println("        .signup-form button:hover {");
        out.println("            background: #6d44b8;");
        out.println("        }");
        out.println("        .login-link {");
        out.println("            color: #fff;");
        out.println("            text-decoration: none;");
        out.println("            margin-top: 5px;");
        out.println("            font-size: 0.9em;");
        out.println("        }");
        out.println("        .login-link:hover {");
        out.println("            text-decoration: underline;");
        out.println("        }");
        out.println("    </style>");
        out.println("    <link href=\"https://fonts.googleapis.com/css2?family=Jost:wght@500&display=swap\" rel=\"stylesheet\">");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class=\"main\">");
        out.println("        <form class=\"signup-form\" action='register' method='POST' xmlns:th='http://www.w3.org/1999/xhtml'>");
        out.println("            <label>Регистрация</label>");
        out.println("            <input type='text' name='username' placeholder='Логин' required>");
        out.println("            <input type='email' name='email' placeholder='Email' required>");
        out.println("            <input type='password' name='password' placeholder='Пароль' required>");
        out.println("            <button type='submit'>Зарегистрироваться</button>");
        out.println("            <a href='login' class=\"login-link\">Уже есть аккаунт?</a>");
        out.println("        </form>");
        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }*/

        String username = HtmlUtils.escapeHtml(req.getParameter("username"));
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
                resp.sendRedirect("register?error=incorrect_password_format");
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