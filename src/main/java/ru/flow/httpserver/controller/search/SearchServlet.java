package ru.flow.httpserver.controller.search;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.services.HtmlBuilderService;
import ru.flow.httpserver.utils.HtmlUtils;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;

@WebServlet("/search")
public class SearchServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Получаем параметры и данные сессии
        String searchQuery = req.getParameter("query");
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        // Проверка авторизации
        if (currentUser == null) {
            resp.sendRedirect("login.html?redirect=search&query=" + URLEncoder.encode(searchQuery, "UTF-8"));
            return;
        }

        // Проверка наличия поискового запроса
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            resp.sendRedirect("mainpage.html?error=empty_query");
            return;
        }

        MySQL db = new MySQL();
        User foundUser = db.findByUsername(searchQuery);

        // Проверка существования пользователя
        if (foundUser == null) {
            resp.sendRedirect("mainpage.html?error=user_not_found&query=" + URLEncoder.encode(searchQuery, "UTF-8"));
            return;
        }

        // Проверка, что это не свой профиль
        if (foundUser.getUsername().equals(currentUser.getUsername())) {
            resp.sendRedirect("profile");
            return;
        }

        // Получаем статус дружбы
        String status = null;

        status = db.getFriendshipStatus(currentUser.getUsername(), foundUser.getUsername());


        // Формируем ответ
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Профиль " + HtmlUtils.escapeHtml(foundUser.getUsername()) + "</title></head><body>");
        out.println("<h2>Профиль пользователя</h2>");
        out.println("<p>Имя: " + HtmlUtils.escapeHtml(foundUser.getUsername()) + "</p>");
        out.println("<p>Социальный рейтинг: " + foundUser.getSocialRating() + "</p>");
        // Блок управления дружбой
        out.println("<div class='friendship-actions'>");
        switch (status) {
            case "NOT_EXISTS":
                out.println("<form action='friendship' method='POST'>");
                out.println("<input type='hidden' name='action' value='send_request'>");
                out.println("<input type='hidden' name='target' value='" + HtmlUtils.escapeHtml(foundUser.getUsername()) + "'>");
                out.println("<button type='submit'>Добавить в друзья</button>");
                out.println("</form>");
                break;

            case "PENDING":
                    if (db.isRequestSender(foundUser.getUsername(), currentUser.getUsername())) {
                        out.println("<p>Ожидает вашего подтверждения</p>");
                        out.println("<form action='friendship' method='POST'>");
                        out.println("<input type='hidden' name='action' value='accept_request'>");

                        out.println("<input type='hidden' name='request_id' value='" + db.getRequestId(foundUser.getUsername(), currentUser.getUsername()) + "'>");
                        out.println("<button type='submit'>Принять</button>");
                        out.println("</form>");
                        out.println("<form action='friendship' method='POST'>");
                        out.println("<input type='hidden' name='action' value='reject_request'>");

                        out.println("<input type='hidden' name='request_id' value='" + db.getRequestId(foundUser.getUsername(), currentUser.getUsername()) + "'>");
                        out.println("<button type='submit'>Отклонить</button>");
                        out.println("</form>");
                    } else {
                        out.println("<p>Запрос отправлен</p>");
                        out.println("<form action='friendship' method='POST'>");
                        out.println("<input type='hidden' name='action' value='cancel_request'>");

                        out.println("<input type='hidden' name='request_id' value='" + db.getRequestId(currentUser.getUsername(), foundUser.getUsername()) + "'>");
                        out.println("<button type='submit'>Отменить запрос</button>");
                        out.println("</form>");
                    }
                break;

            case "ACCEPTED":
                out.println("<p>У вас в друзьях</p>");
                out.println("<form action='friendship' method='POST'>");
                out.println("<input type='hidden' name='action' value='remove_friend'>");
                out.println("<input type='hidden' name='target' value='" + HtmlUtils.escapeHtml(foundUser.getUsername()) + "'>");
                out.println("<button type='submit'>Удалить из друзей</button>");
                out.println("</form>");
                break;

            case "REJECTED":
                out.println("<p>Вы отклонили запрос</p>");
                out.println("<form action='friendship' method='POST'>");
                out.println("<input type='hidden' name='action' value='send_request'>");
                out.println("<input type='hidden' name='target' value='" + HtmlUtils.escapeHtml(foundUser.getUsername()) + "'>");
                out.println("<button type='submit'>Отправить запрос</button>");
                out.println("</form>");
                break;
        }
        out.println("<h2>Посты:</h2>");
        try {
            HtmlBuilderService.renderUserPostsList(out, db, foundUser.getUsername(), req);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        out.println("</div>");

        out.println("</body></html>");
    }

    // Метод для экранирования HTML

}
