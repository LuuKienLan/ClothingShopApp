package com.example.clothingshopapp.data.model;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable { // ⭐ PHẢI CÓ
    private String productId;
    private String name;
    private String category;
    private double basePrice;
    private String description;
    private List<Variant> variants;

    public Product() {}

    // --- Getters ---
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getBasePrice() { return basePrice; }
    public String getDescription() { return description; }
    public List<Variant> getVariants() { return variants; }

    // --- Setters (Cần cho OrderDetailActivity) ---
    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public void setDescription(String description) { this.description = description; }
    public void setVariants(List<Variant> variants) { this.variants = variants; }
}