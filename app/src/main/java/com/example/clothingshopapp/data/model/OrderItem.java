package com.example.clothingshopapp.data.model;

import java.io.Serializable; // ⭐ THÊM IMPORT

public class OrderItem implements Serializable { // ⭐ THÊM "implements Serializable"
    private String productId;
    private String productName;
    private String color;
    private String size;
    private int quantity;
    private double price;
    private String imageUrl;

    public OrderItem() {}

    public OrderItem(String productId, String productName, String color, String size, int quantity, double price, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
}