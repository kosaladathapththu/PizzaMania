package com.kosala.pizza_mania.models;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private String branchId; // nearest branch ID
    private String branchName;
    private List<Pizza> items;
    private double deliveryLat;
    private double deliveryLng;
    private String status; // pending, delivering, delivered

    public Order() {}

    public Order(String orderId, String userId, String branchId, String branchName,
                 List<Pizza> items, double deliveryLat, double deliveryLng, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.branchId = branchId;
        this.branchName = branchName;
        this.items = items;
        this.deliveryLat = deliveryLat;
        this.deliveryLng = deliveryLng;
        this.status = status;
    }

    // Getters & Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public List<Pizza> getItems() { return items; }
    public void setItems(List<Pizza> items) { this.items = items; }

    public double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(double deliveryLat) { this.deliveryLat = deliveryLat; }

    public double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(double deliveryLng) { this.deliveryLng = deliveryLng; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
