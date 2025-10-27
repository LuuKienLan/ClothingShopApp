package com.example.clothingshopapp.data.model;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private long timestamp;
    private double totalAmount;
    private String status; // e.g., "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItem> items;

    public Order() {} // Needed for Firebase

    // Getters
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public long getTimestamp() { return timestamp; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public List<OrderItem> getItems() { return items; }

    // Setters
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}