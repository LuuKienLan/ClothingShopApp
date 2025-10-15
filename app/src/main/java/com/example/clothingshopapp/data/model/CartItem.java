// File: data/model/CartItem.java
package com.example.clothingshopapp.data.model;

public class CartItem {
    private String name;
    private double price; // SỬA: Dùng double thay vì String
    private String size;
    private int quantity;
    private int imageResId; // Tạm thời dùng cho mock data

    // Constructor
    public CartItem(String name, double price, String size, int quantity, int imageResId) {
        this.name = name;
        this.price = price;
        this.size = size;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public int getImageResId() { return imageResId; }

    // Setter
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}