package com.alizzelol.adaechat;

public class User {

    private String username;
    private String email;
    private String userId; // Añadido el campo userId

    public User() {
        // Constructor vacío requerido por Firebase
    }

    public User(String username, String email, String userId) {
        this.username = username;
        this.email = email;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}