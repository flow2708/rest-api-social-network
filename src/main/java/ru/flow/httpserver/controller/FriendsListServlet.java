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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/friendsList")
public class FriendsListServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MySQL db = new MySQL();
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        PrintWriter out = resp.getWriter();

        // Устанавливаем тип содержимого
        resp.setContentType("text/html;charset=UTF-8");

        // Начало HTML-документа
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Список друзей " + HtmlUtils.escapeHtml(currentUser.getUsername()) + "</title>");
        out.println("<style>");
        out.println(".friend { border: 1px solid #ccc; padding: 10px; margin: 10px; border-radius: 5px; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Список друзей</h1>");

        List<String> friendsList;
            friendsList = db.getFriendsList(currentUser.getUsername());

            if (friendsList.isEmpty()) {
                out.println("<p>У вас пока нет друзей.</p>");
            } else {
                for (String friend : friendsList) {
                    User friendUser = db.findByUsername(friend);
                    out.println("<div class='friend'>");
                    out.println("<h2>" + HtmlUtils.escapeHtml(friend) + "</h2>");
                    out.println("<p>Социальный рейтинг: " + friendUser.getSocialRating() + "</p>");
                    out.println("</div>");
                }
            }

        // Завершение HTML-документа
        out.println("</body>");
        out.println("</html>");
    }
}