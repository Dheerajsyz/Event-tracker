package com.dheeraj.snhu_dheeraj_kollapaneni;

import java.io.Serializable;

public class User implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String role;

    public User() {
    }

    public User(String userId, String username, String email, String phone, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
