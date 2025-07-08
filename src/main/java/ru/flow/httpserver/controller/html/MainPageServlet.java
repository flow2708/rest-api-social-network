package ru.flow.httpserver.controller.html;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.flow.httpserver.utils.CsrfUtils;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/mainpage")
public class MainPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='ru'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Главная страница</title>");
        out.println("<style>");
        out.println(".search-box { margin: 20px; padding: 10px; border: 1px solid #ccc; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='header'>");
        out.println("<form action='profile' method='GET'>");
        out.println("<a href='profile'>Мой профиль</a>");
        out.println("</form>");
        out.println("</div>");
        out.println("<div class='friends'>");
        out.println("<a href='friendsList'>Друзья</a>");
        out.println("</div>");
        out.println("<div class='search-box'>");
        out.println("<form action='search' method='GET'>");
        out.println("<input type='text' name='query' placeholder='Поиск пользователей...' required>");
        out.println("<button type='submit'>Найти</button>");
        out.println("</form>");
        out.println("</div>");
        out.println("<div class='notifications'>");
        out.println("<a href='notifications'>Уведомления</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
