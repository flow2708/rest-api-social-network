package ru.flow.httpserver.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.utils.HtmlUtils;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/notifications")
public class NotificationsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MySQL db = new MySQL();
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        PrintWriter out = resp.getWriter();
        String csrfToken = (String) session.getAttribute("csrfToken");

        if(currentUser == null) {
            resp.sendRedirect("login");
            return; // Добавлен return для прерывания выполнения
        }

            List<String> requests = db.getFriendRequestSenders(currentUser.getUsername());
            resp.setContentType("text/html;charset=UTF-8");

            if(requests.isEmpty()) {
                out.println("<p>Нет новых уведомлений</p>");
                return;
            }

            for (String sender : requests) {
                int requestId = db.getRequestId(sender, currentUser.getUsername());

                if(requestId == -1) continue; // Пропускаем невалидные запросы

                out.println("<div class='notification-item'>");
                out.println("<p>Запрос от: " + HtmlUtils.escapeHtml(sender) + "</p>");

                // Форма принятия
                out.println("<form action='friendship' method='POST'>");
                out.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
                out.println("<input type='hidden' name='action' value='accept_request'>");
                out.println("<input type='hidden' name='request_id' value='" + requestId + "'>");
                out.println("<button type='submit'>Принять</button>");
                out.println("</form>");

                // Форма отклонения
                out.println("<form action='friendship' method='POST'>");
                out.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
                out.println("<input type='hidden' name='action' value='reject_request'>");
                out.println("<input type='hidden' name='request_id' value='" + requestId + "'>");
                out.println("<button type='submit'>Отклонить</button>");
                out.println("</form>");

                out.println("</div>");
            }
    }
}

