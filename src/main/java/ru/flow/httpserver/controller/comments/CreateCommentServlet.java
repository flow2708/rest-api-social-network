package ru.flow.httpserver.controller.comments;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.User;
import ru.flow.httpserver.utils.CsrfUtils;
import ru.flow.httpserver.utils.HtmlUtils;

import java.io.IOException;

@WebServlet("/createComment")
public class CreateCommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!CsrfUtils.isValid(req)) {
            resp.sendError(403, "Доступ запрещен");
            return;
        }
        MySQL db = new MySQL();
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");

        int post_id = Integer.parseInt(req.getParameter("post_id"));
        String username = currentUser.getUsername();
        String content = HtmlUtils.escapeHtml(req.getParameter("content"));

        if (currentUser == null) {
            resp.sendRedirect("login");
        }

        if (content == null || content.trim().isEmpty()) {
            resp.sendError(400, "Текст поста не может быть пустым!");
            return;
        }

            if (db.createComment(post_id, username, content)) {
                resp.sendRedirect("comment?post_id=" + post_id);
            } else {
                resp.sendError(500, "Ошибка при создании поста!");
            }
    }


}
