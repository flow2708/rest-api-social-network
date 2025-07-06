package ru.flow.httpserver.dao;

import ru.flow.httpserver.entities.Comment;
import ru.flow.httpserver.entities.Post;
import ru.flow.httpserver.utils.PasswordUtils;
import ru.flow.httpserver.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MySQL {
    public final void initializeTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "username VARCHAR(255) PRIMARY KEY,"
                + "email VARCHAR(255) NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "socialrating INTEGER DEFAULT 0,"
                + "ip_address VARCHAR(45) DEFAULT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS friend_requests ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "sender VARCHAR(255) NOT NULL,"
                + "receiver VARCHAR(255) NOT NULL,"
                + "status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (sender) REFERENCES users(username) ON DELETE CASCADE,"
                + "FOREIGN KEY (receiver) REFERENCES users(username) ON DELETE CASCADE,"
                + "UNIQUE(sender, receiver)) ENGINE=InnoDB");
            stmt.execute("CREATE TABLE IF NOT EXISTS posts ("
                + "post_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(255) NOT NULL,"
                + "content TEXT NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "like_count INT DEFAULT 0,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE) ENGINE=InnoDB");
            stmt.execute("CREATE TABLE IF NOT EXISTS comments ("
                + "comment_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "post_id INTEGER NOT NULL,"
                + "username VARCHAR(255) NOT NULL,"
                + "content VARCHAR(1000) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE) ENGINE=InnoDB");
            stmt.execute("CREATE TABLE IF NOT EXISTS likes ("
                + "like_id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "post_id INTEGER NOT NULL,"
                + "username VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,"
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,"
                + "UNIQUE(post_id, username)) ENGINE=InnoDB");

            System.out.println("Таблицы users, friend_requests, posts, comments, likes проверены/созданы");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        return DataSource.getConnection(); //DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
                        /**|________________________________________________|**/
                        /**|                       users                    |**/
                        /**\________________________________________________/**/
    public boolean saveUser(String username, String email, String password, int socialrating, String ip_address) {
        String insertUser = "INSERT INTO users (username, email, password, socialrating, ip_address) VALUES (?, ?, ?, ?, ?)";
        String hashedPassword = PasswordUtils.hashPassword(password);

        try (Connection conn = getConnection();
            PreparedStatement prstatmt = conn.prepareStatement(insertUser)) {
            initializeTables(); /***закомментировать для неинициализации таблицы***/

            prstatmt.setString(1, username);
            prstatmt.setString(2, email);
            prstatmt.setString(3, hashedPassword);
            prstatmt.setInt(4, socialrating);
            prstatmt.setString(5, ip_address);

            int affectedRows = prstatmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка в методе saveUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String username) {
        String findUser = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(findUser)) {
            prstatmt.setString(1, username);

            try (ResultSet resSet = prstatmt.executeQuery()) {
            if (resSet.next()) {
                return new User(
                        resSet.getString("username"),
                        resSet.getString("email"),
                        resSet.getString("password"),
                        resSet.getInt("socialrating")
                    );
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Ошибка в методе findByUsername: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return null;
    }
                            /**|________________________________________________|**/
                            /**|                 friend_requests                |**/
                            /**\________________________________________________/**/
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
            System.err.println("Ошибка в методе sendFriendRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
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
            System.err.println("Ошибка в методе acceptFriendRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
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
            System.err.println("Ошибка в методе rejectFriendRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
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
            System.err.println("Ошибка в методе cancelFriendRequest: " + e.getMessage());
            e.printStackTrace();
            return false;
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
            System.err.println("Ошибка в методе removeFriend: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getFriendshipStatus(String user1, String user2) {
        String sql = "SELECT status FROM friend_requests WHERE " +
                "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                "ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, user1);
            prstatmt.setString(2, user2);
            prstatmt.setString(3, user2);
            prstatmt.setString(4, user1);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                return resSet.next() ? resSet.getString("status") : "NOT_EXISTS";
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getFriendshipStatus: " + e.getMessage());
            e.printStackTrace();
            return null;
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
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, sender);
            prstatmt.setString(2, receiver);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                return resSet.next() ? resSet.getInt("id") : -1;
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getRequestId: " + e.getMessage());
            e.printStackTrace();
            return -1;
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

            try (ResultSet resSet = prstatmt.executeQuery()) {
                return resSet.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе isRequestSender: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getFriendRequestSenders(String receiver) {
        List<String> senders = new ArrayList<>();
        String sql = "SELECT sender FROM friend_requests WHERE receiver = ? AND status = 'PENDING'";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, receiver);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                while (resSet.next()) {
                    senders.add(resSet.getString("sender"));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getFriendRequestSenders: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
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
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, username);
            prstatmt.setString(2, username);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                while (resSet.next()) {
                    String sender = resSet.getString("sender");
                    String receiver = resSet.getString("receiver");
                    // Добавляем в список противоположного пользователя
                    if (sender.equals(username)) {
                        friends.add(receiver);
                    } else {
                        friends.add(sender);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getFriendsList: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }

        return friends;
    }
                                /**|________________________________________________|**/
                                /**|                      posts                     |**/
                                /**\________________________________________________/**/
    public boolean createPost(String username, String content) {
        String sql = "INSERT INTO posts (username, content) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, username);
            prstatmt.setString(2, content);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе createPost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<Post> getUserPostsList(String username) {
        List<Post> userPostsList = new ArrayList<>();
        String sql = "SELECT post_id, username, content, like_count FROM posts WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setString(1, username);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                while (resSet.next()) {
                    Post post = new Post(
                            resSet.getInt("post_id"),
                            resSet.getString("username"),
                            resSet.getString("content"),
                            resSet.getInt("like_count")
                    );
                    userPostsList.add(post);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getUserPostsList: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
        return userPostsList;
    }
    public synchronized boolean addLikeToPost(int post_id) {
        String sql = "UPDATE posts SET like_count = like_count + 1 WHERE post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе addLikeToPost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public synchronized boolean removeLikeFromPost(int post_id){
        String sql = "UPDATE posts Set like_count = like_count - 1 WHERE post_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе removeLikeFromPost: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
                        /**|________________________________________________|**/
                        /**|                    comments                    |**/
                        /**\________________________________________________/**/
    public boolean createComment(int post_id, String username, String content) {
        String sql = "INSERT INTO comments (post_id, username, content) VALUES (?, ?, ?) ";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);
            prstatmt.setString(3, content);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе createComment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<Comment> getCommentList(int post_id) {
        List<Comment> commentList = new ArrayList<>();
        String sql = "SELECT comment_id, post_id, username, content FROM comments WHERE post_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                while (resSet.next()) {
                    Comment comment = new Comment(
                            resSet.getInt("comment_id"),
                            resSet.getInt("post_id"),
                            resSet.getString("username"),
                            resSet.getString("content")
                    );
                    commentList.add(comment);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе getCommentList: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
        return commentList;
    }
    /**------------------------------------------------------------------------------------------------------------------**/
    /**-------------------------------------------likes--------------------------------------------------------**/
    public synchronized boolean createLike(int post_id, String username) {
        String sql = "INSERT INTO likes (post_id, username) VALUES (?, ?) ";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе createLike: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public synchronized boolean removeLike(int post_id, String username) {
        String sql = "DELETE from likes WHERE post_id = ? AND username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);

            return prstatmt.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе removeLike: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public synchronized boolean isUserLiked(int post_id, String username) {
        String sql = "SELECT 1 FROM likes WHERE post_id = ? AND username = ?";

        try (Connection conn = getConnection();
             PreparedStatement prstatmt = conn.prepareStatement(sql)) {
            prstatmt.setInt(1, post_id);
            prstatmt.setString(2, username);

            try (ResultSet resSet = prstatmt.executeQuery()) {
                return resSet.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Ошибка в методе isUserLiked: " + e.getMessage());
            e.printStackTrace();
            return false;
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