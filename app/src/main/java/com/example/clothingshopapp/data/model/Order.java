package com.example.clothingshopapp.data.model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable { // ⭐ PHẢI CÓ
    private String orderId;
    private String userId;
    private long timestamp;
    private double totalAmount;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private String phoneNumber; // ⭐ 1. THÊM BIẾN NÀY
    private List<OrderItem> items;
    private double latitude;
    private double longitude;
    private double shippingFee;
    private boolean isBuyNow;

    public Order() {} // Constructor rỗng

    // --- Getters ---
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public long getTimestamp() { return timestamp; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPhoneNumber() { return phoneNumber; } // ⭐ 2. THÊM GETTER NÀY
    public List<OrderItem> getItems() { return items; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getShippingFee() { return shippingFee; }
    public boolean isBuyNow() { return isBuyNow; }

    // --- Setters ---
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; } // ⭐ 3. THÊM SETTER NÀY
    public void setItems(List<OrderItem> items) { this.items = items; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    public void setIsBuyNow(boolean buyNow) { isBuyNow = buyNow; }
}