// File: ui/cart/CartActivity.java
package com.example.clothingshopapp.ui.cart;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import java.util.ArrayList;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private TextView subTotalValue, shippingValue, bagTotalValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Khởi tạo ViewModel
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        initViews();
        setupRecyclerView();
        observeViewModel(); // Hàm quan trọng nhất: lắng nghe và cập nhật UI
        setupListeners();
    }

    private void initViews() {
        subTotalValue = findViewById(R.id.sub_total_value);
        shippingValue = findViewById(R.id.shipping_value);
        bagTotalValue = findViewById(R.id.bag_total_value);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.cart_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter nhận một listener để gọi các hàm trong ViewModel
        cartAdapter = new CartAdapter(new ArrayList<>(), new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartViewModel.updateQuantity(item, newQuantity);
            }

            @Override
            public void onItemRemoved(CartItem item) {
                cartViewModel.removeItem(item);
            }
        });
        recyclerView.setAdapter(cartAdapter);
    }

    // Lắng nghe mọi thay đổi từ ViewModel và tự động cập nhật UI
    private void observeViewModel() {
        // Lắng nghe sự thay đổi của danh sách giỏ hàng
        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems != null) {
                cartAdapter.submitList(cartItems);
            }
        });

        // Lắng nghe sự thay đổi của tổng tiền
        cartViewModel.getSubtotal().observe(this, subtotal -> {
            if (subtotal != null) {
                updateTotalsUI(subtotal);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.checkout_button).setOnClickListener(v -> { /*...*/ });
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
    }

    private void updateTotalsUI(double subTotal) {
        double shipping = 6.00; // Phí ship
        double bagTotal = subTotal + shipping;

        subTotalValue.setText(String.format(Locale.US, "$%.2f", subTotal));
        shippingValue.setText(String.format(Locale.US, "$%.2f", shipping));
        bagTotalValue.setText(String.format(Locale.US, "$%.2f", bagTotal));
    }
}