package com.example.android_lab.models;

public class User {
    private String name;
    private String email;
    private String phone;
    private String role;

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setRole(String role) { this.role = role; }
}
