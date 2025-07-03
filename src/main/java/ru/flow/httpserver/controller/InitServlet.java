package ru.flow.httpserver.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import ru.flow.httpserver.dao.MySQL;

@WebServlet(loadOnStartup = 1)
public class InitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        try {
            System.out.println("Инициализация БД...");
            MySQL db = new MySQL();
            db.connect();
            System.out.println("БД успешно инициализирована");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации БД:");
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}