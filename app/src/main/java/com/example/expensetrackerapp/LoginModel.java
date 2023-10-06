package com.example.expensetrackerapp;

public class LoginModel {
    String name;
    String email;
    String username;
    String password;
    private String uid;


    public LoginModel() {

    }

    public LoginModel(String name, String email, String username, String password, String uid) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
