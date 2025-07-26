package ru.flow.httpserver.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;
import ru.flow.httpserver.services.HtmlBuilderService;
import ru.flow.httpserver.utils.HtmlUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect("login.html");
            return;
        }

        User user = (User) session.getAttribute("user");
        MySQL db = new MySQL();
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html;charset=UTF-8");
        try {
            // Обновление счётчика посещений
            Integer visitCounter = (Integer) session.getAttribute("visitCounter");
            visitCounter = (visitCounter == null) ? 1 : visitCounter + 1;
            session.setAttribute("visitCounter", visitCounter);

            // Формирование HTML-структуры
            out.println("<!DOCTYPE html>");
            out.println("<html lang='ru'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Профиль</title>");
            out.println("<style>");
            out.println(".post { margin: 15px 0; padding: 10px; border: 1px solid #ddd; }");
            out.println(".user-avatar { background-color: #3498db; color: white; width: 30px; height: 30px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.printf("<h1>Профиль: %s</h1>%n", HtmlUtils.escapeHtml(user.getUsername()));
            out.printf("<p>Email: %s</p>%n", user.getEmail());
            out.printf("<p>Рейтинг: %d</p>%n", user.getSocialRating());
            out.printf("<p>Посещений: %d</p>%n", visitCounter);
            out.println("<form action='createPost' method='GET'>");
            out.println("<button type=\"submit\">Создать пост</button> | <a href='logout'>Выйти</a>");
            out.println("</form>");
            out.println("<h2>Ваши посты:</h2>");

            // Вывод постов
            HtmlBuilderService.renderUserPostsList(out, db, user.getUsername(), req);

            out.println("</body>");
            out.println("</html>");

        } catch (SQLException | ClassNotFoundException e) {
            out.println("<p style='color:red;'>Ошибка загрузки постов</p>");
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}