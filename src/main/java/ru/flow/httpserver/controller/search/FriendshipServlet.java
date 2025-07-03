package ru.flow.httpserver.controller.search;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/friendship")
public class FriendshipServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                try {
                    if (db.sendFriendRequest(currentUser.getUsername(), targetUsername)) {
                        session.setAttribute("message", "Запрос отправлен!");
                        out.println("<p>Запрос в друзья отправлен!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка отправки запроса");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                out.println("<p>Запрос дружбы отправлен!</p>");

                break;
            case "accept_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));
                try {
                    if(db.acceptFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос принят!");
                        out.println("<p>Запрос в друзья принят!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка принятия запроса");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "reject_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));
                try {
                    if(db.rejectFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос отклонен!");
                        out.println("<p>Запрос в друзья отклонен!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка отклонения запроса");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "cancel_request":
                requestId = Integer.parseInt(req.getParameter("request_id"));
                try {
                    if (db.cancelFriendRequest(requestId, currentUser.getUsername())) {
                        session.setAttribute("message", "Запрос отменен!");
                        out.println("<p>Запрос в друзья отменён!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка омены запроса");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "remove_friend":
                try {
                    if (db.removeFriend(currentUser.getUsername(), targetUsername)) {
                        session.setAttribute("message", "Пользователь исключен из друзей!");
                        out.println("<p>Пользователь удалён из друзей!</p>");
                    } else {
                        session.setAttribute("error", "Ошибка удаления");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }
}
