package com.example.clothingshopapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.ui.adapter.CartAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    private TextView subTotalValue, shippingValue, bagTotalValue;
    private RecyclerView recyclerView;
    private LinearLayout emptyCartLayout;
    private Button continueShoppingButton;
    private Group checkoutGroup;
    private TextView clearAllButton;

    // ⭐ XÓA CÁC BIẾN isBuyNowFlow và buyNowList ⭐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // ⭐ XÓA LOGIC KIỂM TRA INTENT "IS_BUY_NOW" ⭐

        initViews();
        setupRecyclerView();

        // ⭐ CHỈ CÒN LUỒNG GIỎ HÀNG CHUNG ⭐
        Log.d("CartActivity", "Chế độ Giỏ hàng (Regular Cart)");
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        observeViewModel();

        setupListeners();
    }

    private void initViews() {
        subTotalValue = findViewById(R.id.sub_total_value);
        shippingValue = findViewById(R.id.shipping_value);
        bagTotalValue = findViewById(R.id.bag_total_value);
        recyclerView = findViewById(R.id.cart_recycler_view);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);
        checkoutGroup = findViewById(R.id.checkoutGroup);
        clearAllButton = findViewById(R.id.clearAllButton);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CartAdapter.CartItemListener listener = new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                // ⭐ XÓA LOGIC isBuyNowFlow ⭐
                cartViewModel.updateQuantity(item, newQuantity);
            }

            @Override
            public void onItemRemoved(CartItem item) {
                // ⭐ XÓA LOGIC isBuyNowFlow ⭐
                cartViewModel.removeItem(item);
            }
        };

        cartAdapter = new CartAdapter(listener);
        recyclerView.setAdapter(cartAdapter);
    }

    // ⭐ XÓA HÀM handleBuyNowFlow() ⭐

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                checkoutGroup.setVisibility(View.GONE);
                emptyCartLayout.setVisibility(View.VISIBLE);
                clearAllButton.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                checkoutGroup.setVisibility(View.VISIBLE);
                emptyCartLayout.setVisibility(View.GONE);
                clearAllButton.setVisibility(View.VISIBLE);
                cartAdapter.submitList(cartItems);
            }
        });

        cartViewModel.getSubtotal().observe(this, this::updateTotalsUI);
    }

    private void setupListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        continueShoppingButton.setOnClickListener(v -> finish());

        clearAllButton.setOnClickListener(v -> {
            // ⭐ XÓA KIỂM TRA isBuyNowFlow ⭐
            if (cartViewModel != null) {
                showClearCartConfirmationDialog();
            }
        });

        findViewById(R.id.checkout_button).setOnClickListener(v -> {
            // ⭐ CHỈ CÒN LOGIC LẤY TỪ VIEWMODEL ⭐
            List<CartItem> itemsToCheckout = cartViewModel.getCartItems().getValue();

            if (itemsToCheckout == null || itemsToCheckout.isEmpty()) {
                Toast.makeText(this, "Không có sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            // Gửi list đi (phải ép kiểu về ArrayList)
            intent.putExtra("CHECKOUT_ITEMS", new ArrayList<>(itemsToCheckout));
            intent.putExtra("IS_BUY_NOW", false); // Luôn là false từ giỏ hàng
            startActivity(intent);
        });
    }

    private void showClearCartConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng không?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    CartManager.getInstance().clearCart();
                    Toast.makeText(this, "Đã xóa tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // ⭐ XÓA HÀM calculateSubtotal() (Không cần nữa) ⭐

    private void updateTotalsUI(double subTotal) {
        double shipping = (subTotal > 0) ? 30000 : 0.00;
        double bagTotal = subTotal + shipping;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        subTotalValue.setText(formatter.format(subTotal));
        shippingValue.setText(formatter.format(shipping));
        bagTotalValue.setText(formatter.format(bagTotal));
    }
}