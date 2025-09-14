package com.kosala.pizza_mania;

public class OrderModel {
    private String orderId;
    private String customerId;
    private double totalPrice;
    private String status;

    public OrderModel(String orderId, String customerId, double totalPrice, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
}
