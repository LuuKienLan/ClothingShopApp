package com.example.clothingshopapp.data.model;

import java.io.Serializable; // ⭐ THÊM IMPORT

public class Size implements Serializable { // ⭐ THÊM "implements Serializable"
    private String size;
    private int stock;

    public Size() {}

    // Getters
    public String getSize() { return size; }
    public int getStock() { return stock; }
}