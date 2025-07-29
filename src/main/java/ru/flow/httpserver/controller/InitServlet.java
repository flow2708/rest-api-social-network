package ru.flow.httpserver.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import ru.flow.httpserver.dao.DataSource;
import ru.flow.httpserver.dao.MySQL;

import java.sql.Connection;

@WebServlet(loadOnStartup = 1)
public class InitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        try {
            System.out.println("Инициализация БД...");

            try (Connection testConn = MySQL.getConnection()) {
                System.out.println("Подключение к БД успешно");
            }

            MySQL db = new MySQL();
            db.initializeTables();
            System.out.println("БД успешно инициализирована");
        } catch (Exception e) {
            System.err.println("Критическая ошибка инициализации БД:");
            e.printStackTrace()
            throw new ServletException("Ошибка инициализации БД: " + e.getMessage(), e);
        }
    }
    @Override
    public void destroy() {
        DataSource.close();
    }
}