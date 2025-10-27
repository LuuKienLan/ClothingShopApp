package com.example.clothingshopapp.data.model;

public class OrderItem {
    private String productId;
    private String productName;
    private String color;
    private String size;
    private int quantity;
    private double price; // Price at the time of order
    private String imageUrl;

    public OrderItem() {} // Needed for Firebase

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