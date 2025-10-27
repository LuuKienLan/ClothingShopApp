package com.example.clothingshopapp.data.model;

import java.util.List;

public class Product {
    // Thuộc tính phải khớp với tên trong file JSON
    private String productId;
    private String name;
    private String category;
    private double basePrice;
    private String description;
    private List<Variant> variants;

    // Firebase yêu cầu một constructor rỗng để đọc dữ liệu
    public Product() {}

    // --- Getters ---
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getBasePrice() { return basePrice; }
    public String getDescription() { return description; }
    public List<Variant> getVariants() { return variants; }

    // --- Setter ---
    public void setProductId(String productId) { this.productId = productId; }
}