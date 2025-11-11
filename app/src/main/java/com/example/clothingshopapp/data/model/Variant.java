package com.example.clothingshopapp.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Variant implements Serializable { // ⭐ PHẢI CÓ
    private String color;
    private List<Size> sizes;

    // ⭐ SỬA 1: Đổi String thành List<String>
    private List<String> imageUrls;

    public Variant() {
        // Khởi tạo list rỗng để tránh lỗi Null
        this.imageUrls = new ArrayList<>();
        this.sizes = new ArrayList<>();
    }

    // Constructor cũ (Sửa lại cho khớp)
    public Variant(String color, String imageUrl) {
        this.color = color;
        this.imageUrls = new ArrayList<>();
        if (imageUrl != null) {
            this.imageUrls.add(imageUrl); // Thêm ảnh cũ vào list
        }
        this.sizes = new ArrayList<>();
    }

    // --- Getters ---
    public String getColor() { return color; }
    public List<Size> getSizes() { return sizes; }

    // ⭐ SỬA 2: Sửa Getter
    public List<String> getImageUrls() { return imageUrls; }

    // ⭐ SỬA 3: Thêm 1 hàm "tiện ích" (để lấy ảnh đầu tiên làm ảnh bìa)
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null; // Hoặc trả về 1 link ảnh placeholder
    }
}