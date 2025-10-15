// File: ui/cart/CartViewModel.java
package com.example.clothingshopapp.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.clothingshopapp.data.CartManager;
import com.example.clothingshopapp.data.model.CartItem;
import java.util.List;

public class CartViewModel extends ViewModel {

    private CartManager cartManager;
    private MutableLiveData<List<CartItem>> cartItemsLiveData = new MutableLiveData<>();
    private MutableLiveData<Double> subtotalLiveData = new MutableLiveData<>();

    public CartViewModel() {
        cartManager = CartManager.getInstance();
        loadCartData();
    }

    // Nạp dữ liệu từ Manager và cập nhật LiveData
    private void loadCartData() {
        cartItemsLiveData.setValue(cartManager.getCartItems());
        calculateTotals();
    }

    private void calculateTotals() {
        subtotalLiveData.setValue(cartManager.getSubtotal());
    }

    // Các hàm này được Activity gọi khi người dùng tương tác
    public void removeItem(CartItem item) {
        cartManager.removeItem(item);
        // Sau khi thay đổi dữ liệu gốc, nạp lại để LiveData được cập nhật
        loadCartData();
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        // 1. Yêu cầu Manager cập nhật dữ liệu gốc
        cartManager.updateQuantity(item, newQuantity);

        // 2. Tính lại tổng tiền và cập nhật LiveData của nó
        calculateTotals();

        // 3. THÊM DÒNG NÀY: Đây là bước quan trọng bị thiếu
        // Báo cho cartItemsLiveData biết rằng nội dung bên trong danh sách đã thay đổi
        // và cần được cập nhật lại trên giao diện.
        cartItemsLiveData.setValue(cartManager.getCartItems());
    }

    // Cung cấp LiveData để Activity "lắng nghe"
    public LiveData<List<CartItem>> getCartItems() {
        return cartItemsLiveData;
    }

    public LiveData<Double> getSubtotal() {
        return subtotalLiveData;
    }
}