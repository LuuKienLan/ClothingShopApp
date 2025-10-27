package com.example.clothingshopapp.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.remote.CartManager;
import java.util.List;

public class CartViewModel extends ViewModel {

    private final CartManager cartManager;
    private final MediatorLiveData<Double> subtotalLiveData = new MediatorLiveData<>();
    private final LiveData<List<CartItem>> cartItemsLiveData;

    public CartViewModel() {
        cartManager = CartManager.getInstance();
        cartItemsLiveData = cartManager.getCartItemsLiveData();

        // Lắng nghe danh sách giỏ hàng. Mỗi khi nó thay đổi, tính lại tổng tiền.
        subtotalLiveData.addSource(cartItemsLiveData, cartItems -> {
            calculateAndPostSubtotal(cartItems);
        });
    }

    private void calculateAndPostSubtotal(List<CartItem> cartItems) {
        double subtotal = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                subtotal += item.getProduct().getBasePrice() * item.getQuantity();
            }
        }
        subtotalLiveData.setValue(subtotal);
    }

    // Các hàm này giờ chỉ cần gọi đến CartManager
    public void removeItem(CartItem item) {
        cartManager.removeItem(item);
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        cartManager.updateQuantity(item, newQuantity);
    }

    // Trả về LiveData trực tiếp từ CartManager
    public LiveData<List<CartItem>> getCartItems() {
        return cartItemsLiveData;
    }

    public LiveData<Double> getSubtotal() {
        return subtotalLiveData;
    }
}