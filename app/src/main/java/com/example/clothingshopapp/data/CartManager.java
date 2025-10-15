// File: data/CartManager.java
package com.example.clothingshopapp.data;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {
        // Thêm dữ liệu giả ở đây để test
        if (cartItems.isEmpty()) {
            cartItems.add(new CartItem("Cotton queen T", 43.00, "S", 1, R.drawable.mock_tshirt_main));
            cartItems.add(new CartItem("Grey T-shirt", 41.00, "M", 2, R.drawable.mock_tshirt_main));
            cartItems.add(new CartItem("Black Hoodie", 75.00, "L", 1, R.drawable.mock_tshirt_main));
        }
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(CartItem newItem) {
        // Duyệt qua danh sách các sản phẩm hiện có trong giỏ hàng
        for (CartItem existingItem : cartItems) {
            // Một sản phẩm được coi là "trùng lặp" nếu có cùng TÊN và cùng SIZE
            // Sau này khi có API, bạn sẽ so sánh theo Product ID cho chính xác tuyệt đối
            if (existingItem.getName().equals(newItem.getName()) && existingItem.getSize().equals(newItem.getSize())) {

                // Nếu tìm thấy sản phẩm trùng lặp, cập nhật số lượng
                int oldQuantity = existingItem.getQuantity();
                int newQuantity = oldQuantity + newItem.getQuantity();
                existingItem.setQuantity(newQuantity);

                // Đã xử lý xong, thoát khỏi hàm để không thực hiện việc thêm mới ở dưới
                return;
            }
        }

        // Nếu vòng lặp kết thúc mà không tìm thấy sản phẩm nào trùng lặp,
        // thì thêm sản phẩm mới này vào danh sách.
        cartItems.add(newItem);
    }

    public void removeItem(CartItem itemToRemove) {
        cartItems.remove(itemToRemove);
    }


    public void updateQuantity(CartItem itemToUpdate, int newQuantity) {
        for (CartItem item : cartItems) {
            // Tìm đúng item trong giỏ hàng và cập nhật số lượng
            if (item.getName().equals(itemToUpdate.getName()) && item.getSize().equals(itemToUpdate.getSize())) {
                item.setQuantity(newQuantity);
                break;
            }
        }
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        return subtotal;
    }

    public void clearCart() {
        cartItems.clear();
    }
}