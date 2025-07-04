package ru.flow.httpserver.dao;

import ru.flow.httpserver.entities.Comment;
import ru.flow.httpserver.entities.Post;
import ru.flow.httpserver.utils.PasswordUtils;
import ru.flow.httpserver.entities.User;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQL {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/social_network?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "social_network_admin";
    private static final String DB_PASSWORD = "Superamin020304";

    // Метод для создания таблицы users
    private static void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "username VARCHAR(255) NOT NULL UNIQUE,"
                + "email VARCHAR(255) NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "socialrating INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS friend_requests ("
                + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "sender VARCHAR(255) NOT NULL,"
                + "receiver VARCHAR(255) NOT NULL,"
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'ACCEPTED', 'REJECTED')),"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (sender) REFERENCES users(username) ON DELETE CASCADE,"
                + "FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE CASCADE,"
                + "UNIQUE(sender, receiver))");
            stmt.execute("CREATE TABLE IF NOT EXISTS posts ("
                + "post_id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(255) NOT NULL,"
                + "content VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "like_count INTEGER DEFAULT 0,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS comments ("
                + "comment_id INTEGER PRIMARY KEY AUTO_INCREMENT ,"
                + "post_id INTEGER NOT NULL,"
                + "username VARCHAR(255) NOT NULL,"
                + "content VARCHAR(1000) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS likes ("
                + "like_id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "post_id INTEGER NOT NULL,"
                + "username VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,"
                + "UNIQUE(post_id, username))");

            System.out.println("Таблицы users, friend_requests, posts, comments, likes проверены/созданы");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    /**-------------------------------------------users--------------------------------------------------------**/
    public boolean saveUser(String username, String email, String password, int socialrating) {
        String insertUser = "INSERT INTO users (username, email, password, socialrating) VALUES (?, ?, ?, ?)";
        String hashedPassword = PasswordUtils.hashPassword(password);

        try (Connection conn = getConnection();
            PreparedStatement prstatmt = conn.prepareStatement(insertUser)) {

            prstatmt.setString(1, username);
            prstatmt.setString(2, email);
            prstatmt.setString(3, hashedPassword);
            prstatmt.setInt(4, socialrating);

            int affectedRows = prstatmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при сохранении пользователя: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String username) {
        String findUser = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(findUser);
             ResultSet resSet = prstatmt.executeQuery()) {

            prstatmt.setString(1, username);


            if (resSet.next()) {
                return new User(
                        resSet.getString("username"),
                        resSet.getString("email"),
                        resSet.getString("password"),
                        resSet.getInt("socialrating")
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка при поиске пользователя: " + e.getMessage());
        }
        return null;
    }
    /**------------------------------------------------------------------------------------------------------------------**/
    /**-------------------------------------------friend_requests--------------------------------------------------------**/
    public boolean sendFriendRequest(String sender, String receiver) {
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Нельзя отправить запрос самому себе");
        }

        String sql = "INSERT INTO friend_requests (sender, receiver, status) VALUES (?, ?, 'PENDING')";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, sender);
            prstatmt.setString(2, receiver);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean acceptFriendRequest(int requestId, String receiver) {
        // Убедимся, что соединение активно
        String sql = "UPDATE friend_requests SET status = 'ACCEPTED' " +
                "WHERE id = ? AND receiver = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, requestId);
            prstatmt.setString(2, receiver);
            int updated = prstatmt.executeUpdate();

            if (updated == 0) {
                System.err.println("Не удалось обновить запрос. Возможные причины:");
                System.err.println("- Неправильный ID запроса: " + requestId);
                System.err.println("- Получатель не совпадает: " + receiver);
                System.err.println("- Запрос уже не в статусе PENDING");
            }

            return updated > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Отклонить запрос
    public boolean rejectFriendRequest(int requestId, String receiver) {
        String sql = "UPDATE friend_requests SET status = 'REJECTED' " +
                "WHERE id = ? AND receiver = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, requestId);
            prstatmt.setString(2, receiver);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean cancelFriendRequest(int requestId, String senderUsername) {
        String sql = "DELETE FROM friend_requests WHERE id = ? AND sender = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, requestId);
            prstatmt.setString(2, senderUsername);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean removeFriend(String user1, String user2) {
        String sql = "DELETE FROM friend_requests WHERE " +
                "((sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)) " +
                "AND status = 'ACCEPTED'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, user1);
            prstatmt.setString(2, user2);
            prstatmt.setString(3, user2);
            prstatmt.setString(4, user1);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFriendshipStatus(String user1, String user2) {
        String sql = "SELECT status FROM friend_requests WHERE " +
                "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                "ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery()) {
            prstatmt.setString(1, user1);
            prstatmt.setString(2, user2);
            prstatmt.setString(3, user2);
            prstatmt.setString(4, user1);

            return rs.next() ? rs.getString("status") : "NOT_EXISTS";
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Получает ID активного запроса в друзья между пользователями
     * @param sender Отправитель запроса
     * @param receiver Получатель запроса
     * @return ID запроса или -1 если не найден
     */
    public int getRequestId(String sender, String receiver) {
        String sql = "SELECT id FROM friend_requests " +
                "WHERE sender = ? AND receiver = ? AND status = 'PENDING' LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery()) {
            prstatmt.setString(1, sender);
            prstatmt.setString(2, receiver);

            return rs.next() ? rs.getInt("id") : -1;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяет, является ли пользователь отправителем запроса
     * @param potentialSender Проверяемый отправитель
     * @param receiver Получатель
     * @return true если пользователь - отправитель активного запроса
     */
    public boolean isRequestSender(String potentialSender, String receiver) {
        String sql = "SELECT 1 FROM friend_requests " +
                "WHERE sender = ? AND receiver = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, potentialSender);
            prstatmt.setString(2, receiver);
            return prstatmt.executeQuery().next();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> getFriendRequestSenders(String receiver) {
        List<String> senders = new ArrayList<>();
        String sql = "SELECT sender FROM friend_requests WHERE receiver = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery();) {
            prstatmt.setString(1, receiver);

            while (rs.next()) {
                senders.add(rs.getString("sender"));
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

        return senders;
    }
    /**
     * Получает список друзей пользователя (взаимно принятые заявки)
     * @param username имя пользователя для которого ищем друзей
     * @return список имен друзей
     */
    public List<String> getFriendsList(String username) {
        List<String> friends = new ArrayList<>();
        String sql = "SELECT sender, receiver FROM friend_requests " +
                "WHERE (sender = ? OR receiver = ?) AND status = 'ACCEPTED'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery()) {
            prstatmt.setString(1, username);
            prstatmt.setString(2, username);

            while (rs.next()) {
                String sender = rs.getString("sender");
                String receiver = rs.getString("receiver");
                // Добавляем в список противоположного пользователя
                if (sender.equals(username)) {
                    friends.add(receiver);
                } else {
                    friends.add(sender);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }

        return friends;
    }
    /**------------------------------------------------------------------------------------------------------------------**/
    /**-------------------------------------------posts--------------------------------------------------------**/
    public boolean createPost(String username, String content) {
        String sql = "INSERT INTO posts (username, content) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, username);
            prstatmt.setString(2, content);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Post> getUserPostsList(String username) {
        List<Post> userPostsList = new ArrayList<>();
        String sql = "SELECT post_id, username, content, like_count FROM posts WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery()) {
            prstatmt.setString(1, username);
                while (rs.next()) {
                    Post post = new Post(
                            rs.getInt("post_id"),
                            rs.getString("username"),
                            rs.getString("content"),
                            rs.getInt("like_count")
                    );
                    userPostsList.add(post);
                }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return userPostsList;
    }
    public boolean addLikeToPost(int post_id) {
        String sql = "UPDATE posts SET like_count = like_count + 1 WHERE post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean removeLikeFromPost(int post_id){
        String sql = "UPDATE posts Set like_count = like_count - 1 WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**------------------------------------------------------------------------------------------------------------------**/
    /**-------------------------------------------comments--------------------------------------------------------**/
    public boolean createComment(int post_id, String username, String content) {
        String sql = "INSERT INTO comments (post_id, username, content) VALUES (?, ?, ?) ";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);
            prstatmt.setString(3, content);
            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Comment> getCommentList(int post_id) {
        List<Comment> commentList = new ArrayList<>();
        String sql = "SELECT post_id, username, content FROM comments WHERE post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql);
             ResultSet rs = prstatmt.executeQuery()) {
            prstatmt.setInt(1, post_id);
                while (rs.next()) {
                    Comment comment = new Comment(
                            rs.getInt("post_id"),
                            rs.getString("username"),
                            rs.getString("content")
                    );
                    commentList.add(comment);
                }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return commentList;
    }
    /**------------------------------------------------------------------------------------------------------------------**/
    /**-------------------------------------------likes--------------------------------------------------------**/
    public boolean createLike(int post_id, String username) {
        String sql = "INSERT INTO likes (post_id, username) VALUES (?, ?) ";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean removeLike(int post_id, String username) {
        String sql = "DELETE from likes WHERE post_id = ? AND username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean isUserLiked(int post_id, String username) {
        String sql = "SELECT 1 FROM likes WHERE post_id = ? AND username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);
            return prstatmt.executeQuery().next();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**------------------------------------------------------------------------------------------------------------------**/

    /* Вспомогательные методы для закрытия ресурсов
    private static void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии statement: " + e.getMessage());
            }
        }
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии result set: " + e.getMessage());
            }
        }
    }
    
     */
}