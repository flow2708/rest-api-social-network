package ru.flow.httpserver.controller.comments;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.flow.httpserver.dao.MySQL;
import ru.flow.httpserver.services.HtmlBuilderService;
import ru.flow.httpserver.utils.HtmlUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MySQL db = new MySQL();
        String post_id = req.getParameter("post_id");
        PrintWriter out = resp.getWriter();

        if (post_id == null || post_id.isEmpty()) {
            // Перенаправляем на страницу с постом ID=1 (или другим дефолтным)
            resp.sendRedirect(req.getContextPath() + "/comment?post_id=1");
            return;
        }

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"ru\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("  <title>Комментарии</title>");
        out.println("  <style>");
        out.println("    body {");
        out.println("      font-family: Arial, sans-serif;");
        out.println("      max-width: 600px;");
        out.println("      margin: 0 auto;");
        out.println("      padding: 20px;");
        out.println("      background-color: #f5f5f5;");
        out.println("    }");
        out.println("    h1 {");
        out.println("      text-align: center;");
        out.println("      color: #333;");
        out.println("    }");
        out.println("    .post-form {");
        out.println("      background: white;");
        out.println("      padding: 20px;");
        out.println("      border-radius: 8px;");
        out.println("      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);");
        out.println("    }");
        out.println("    textarea {");
        out.println("      width: 100%;");
        out.println("      padding: 10px;");
        out.println("      border: 1px solid #ddd;");
        out.println("      border-radius: 4px;");
        out.println("      resize: vertical;");
        out.println("      min-height: 100px;");
        out.println("      margin-bottom: 10px;");
        out.println("    }");
        out.println("    button {");
        out.println("      background-color: #4CAF50;");
        out.println("      color: white;");
        out.println("      border: none;");
        out.println("      padding: 10px 15px;");
        out.println("      border-radius: 4px;");
        out.println("      cursor: pointer;");
        out.println("      font-size: 16px;");
        out.println("    }");
        out.println("    button:hover {");
        out.println("      background-color: #45a049;");
        out.println("    }");
        out.println("    .error {");
        out.println("      color: red;");
        out.println("      margin-top: 10px;");
        out.println("    }");
        out.println("    .success {");
        out.println("      color: green;");
        out.println("      margin-top: 10px;");
        out.println("    }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");

        out.println("<div class='comment-form'>");
        out.println("<form action='createComment' method='POST'>");
        out.println("<input type='hidden' name='post_id' value='" + HtmlUtils.escapeHtml(post_id) + "'>");
        out.println("<div>");
        out.println("<textarea name='content' placeholder='Напишите что-нибудь...' required></textarea>");
        out.println("</div>");
        out.println("<button type='submit'>Отправить комментарий</button>");
        out.println("</form>");
        out.println("</div>");

        try {
            HtmlBuilderService.renderCommentList(out, db, Integer.parseInt(post_id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        out.println("</body>");
        out.println("</html>");
    }
}
