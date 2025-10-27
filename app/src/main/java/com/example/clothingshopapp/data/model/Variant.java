package com.example.clothingshopapp.data.model;

import java.util.List;

public class Variant {
    private String color;
    private List<Size> sizes; // THAY THẾ "stock"
    private String imageUrl;

    public Variant() {}

    // Getters
    public String getColor() { return color; }
    public List<Size> getSizes() { return sizes; }
    public String getImageUrl() { return imageUrl; }
}