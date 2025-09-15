package com.kosala.pizza_mania;

public class UserModel {
    private String id;
    private String name;
    private String email;

    public UserModel() {} // Firestore needs empty constructor

    public UserModel(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
