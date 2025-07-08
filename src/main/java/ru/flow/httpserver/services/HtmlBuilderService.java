package ru.flow.httpserver.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.entities.Comment;
import ru.flow.httpserver.entities.Post;
import ru.flow.httpserver.entities.User;
import ru.flow.httpserver.utils.HtmlUtils;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class HtmlBuilderService {
    public static void renderUserPostsList(PrintWriter out, MySQL db, String username, HttpServletRequest req)
            throws SQLException, ClassNotFoundException {

        List<Post> posts = db.getUserPostsList(username);
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        String csrfToken = (String) session.getAttribute("csrfToken");

        if (posts == null || posts.isEmpty()) {
            out.println("<p>Нет постов</p>");
            return;
        }

        out.println("<div class='posts'>");
        for (Post post : posts) {
            String firstLetter = post.getUsername().substring(0, 1).toUpperCase();
            boolean isLiked = db.isUserLiked(post.getPost_id(), user.getUsername()); // Проверяем, лайкнул ли пользователь пост

            out.println("<div class='post'>");
            out.printf("<div class='user-avatar'>%s</div>", firstLetter);
            out.printf("<h3>%s</h3>", post.getUsername());
            out.printf("<p>%s</p>", post.getContent());

            // Форма для лайка (отправляется на сервлет /like)
            out.println("<form class='like-form' action='like' method='POST'>");
            out.println("<input type='hidden' name='csrfToken' value='" + csrfToken + "'>");
            out.println("<input type='hidden' name='post_id' value='" + post.getPost_id() + "'>");
            out.println("<input type='hidden' name='action' value='" + (isLiked ? "unlike" : "like") + "'>");
            out.println("<button type='submit' class='like-btn " + (isLiked ? "liked" : "") + "'>❤️</button>");
            out.printf("<span class='like-count'>%d</span>", post.getLike_count());
            out.println("</form>");

            // Форма для комментариев (ваш существующий код)
            out.println("<form action='comment' method='GET'>");
            out.println("<input type='hidden' name='post_id' value='" + HtmlUtils.escapeHtml(String.valueOf(post.getPost_id())) + "'>");
            out.println("<button class='comment-btn'>");
            out.println("<svg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='currentColor' viewBox='0 0 16 16'>");
            out.println("<path d='M14 1a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1h-2.5a2 2 0 0 0-1.6.8L8 14.333 6.1 11.8a2 2 0 0 0-1.6-.8H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h12zM2 0a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2.5a1 1 0 0 1 .8.4l1.9 2.533a1 1 0 0 0 1.6 0l1.9-2.533a1 1 0 0 1 .8-.4H14a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H2z'/>");
            out.println("</svg>");
            out.println(" Комментарии");
            out.println("</button>");
            out.println("</form>");

            out.println("</div>"); // Закрываем div.post
        }
        out.println("</div>");
    }
    public static void renderCommentList(PrintWriter out, MySQL db, int post_id) throws SQLException, ClassNotFoundException {
        List<Comment> comments = db.getCommentList(post_id);

        if (comments == null || comments.isEmpty()) {
            out.println("<p>Нет комментариев</p>");
            return;
        }

        out.println("<div class='comments'>");
        for (Comment comment : comments) {
            out.println("<div class='comment'>");
            out.printf("<h3>%s</h3>", comment.getUsername());
            out.printf("<p>%s</p>", comment.getContent());
            out.println("</div>");
        }
        out.println("</div>");
    }
}
