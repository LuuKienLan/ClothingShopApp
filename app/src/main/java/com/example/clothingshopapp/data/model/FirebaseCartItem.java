package com.example.clothingshopapp.data.model;

/**
 * Model này chỉ dùng để đọc/ghi dữ liệu giỏ hàng từ Firebase.
 */
public class FirebaseCartItem {
    private String productId;
    private String color;
    private String size;
    private int quantity;
    private String firebaseKey; // Biến tạm để gán key khi đọc

    // Constructor rỗng (Bắt buộc cho Firebase)
    public FirebaseCartItem() {}

    // Constructor đầy đủ (Dùng khi tạo item mới để đẩy lên Firebase)
    public FirebaseCartItem(String productId, String color, String size, int quantity) {
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
    }

    // --- Getters ---
    public String getProductId() { return productId; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public String getFirebaseKey() { return firebaseKey; }

    // --- Setters ---
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}