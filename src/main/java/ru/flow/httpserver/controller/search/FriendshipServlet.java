package ru.flow.httpserver.controller.search;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;
import ru.flow.httpserver.utils.CsrfUtils;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/friendship")
public class FriendshipServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }
        MySQL db = new MySQL();
        String action = req.getParameter("action");
        String targetUsername = req.getParameter("target");
        int requestId;
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        PrintWriter out = resp.getWriter();

        if (currentUser == null) {
            resp.sendRedirect("register.html");
        }

        resp.setContentType("text/html;charset=UTF-8");

        switch (action) {
            case "send_request":
                    if (db.sendFriendRequest(currentUser.getUsername(), targetUsername)) {
                        session.setAttribute("message", "Запрос отправлен!");
                        out.println("<p>Запрос в друзья отправлен!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка отправки запроса");
                    }

                out.println("<p>Запрос дружбы отправлен!</p>");

                break;
            case "accept_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));

                    if(db.acceptFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос принят!");
                        out.println("<p>Запрос в друзья принят!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка принятия запроса");
                    }
                break;
            case "reject_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));
                    if(db.rejectFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос отклонен!");
                        out.println("<p>Запрос в друзья отклонен!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка отклонения запроса");
                    }
                break;
            case "cancel_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));
                    if (db.cancelFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос отменен!");
                        out.println("<p>Запрос в друзья отменён!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка омены запроса");
                    }
                break;
            case "remove_friend":
                    if (db.removeFriend(currentUser.getUsername(), targetUsername)) {
                        session.setAttribute("message", "Пользователь исключен из друзей!");
                        out.println("<p>Пользователь удалён из друзей!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка удаления");
                    }
                break;
        }
    }
}
