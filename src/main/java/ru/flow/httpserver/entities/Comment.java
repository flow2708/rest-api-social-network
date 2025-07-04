package ru.flow.httpserver.entities;

public class Comment {
    int comment_id;
    int post_id;
    String username;
    String content;
    public Comment (int comment_id, int post_id, String username, String content) {
        this.comment_id = comment_id;
        this.post_id = post_id;
        this.username = username;
        this.content = content;
    }
    public int getComment_id() {
        return this.comment_id;
    }
    public int getPost_id() {
        return this.post_id;
    }
    public String getUsername() {
        return this.username;
    }
    public String getContent() {
        return this.content;
    }
}
