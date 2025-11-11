package com.example.clothingshopapp.data.model;

import java.io.Serializable; // ⭐ THÊM

public class CartItem implements Serializable { // ⭐ THÊM
    private String firebaseKey;
    private Product product;
    private Variant variant;
    private int quantity;
    private String size;

    public CartItem(Product product, Variant variant, int quantity, String size) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.size = size;
    }

    // Getters
    public String getFirebaseKey() { return firebaseKey; }
    public Product getProduct() { return product; }
    public Variant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public String getSize() { return size; }

    // Setters
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}