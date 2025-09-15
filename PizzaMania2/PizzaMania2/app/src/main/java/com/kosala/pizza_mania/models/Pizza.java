package com.kosala.pizza_mania.models;

import java.io.Serializable;

public class Pizza implements Serializable {

    private String id;          // Firestore document ID
    private String name;
    private double price;
    private String imageUrl;
    private int quantity;       // for cart only

    /** No-args constructor required by Firestore */
    public Pizza() { }

    /** Constructor for Firestore pizzas (no quantity) */
    public Pizza(String name, double price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = 1; // default 1
    }

    /** Constructor for SQLite cart with quantity */
    public Pizza(String name, double price, String imageUrl, int quantity) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
    }

    // 🔹 Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
