package com.example.clothingshopapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.ui.adapter.CartAdapter;
import java.text.NumberFormat;
import java.util.Locale;


public class CartActivity extends AppCompatActivity {

    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;

    // UI Views
    private TextView subTotalValue, shippingValue, bagTotalValue;
    private RecyclerView recyclerView;
    private LinearLayout emptyCartLayout;
    private Button continueShoppingButton;
    private Group checkoutGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        initViews();
        setupRecyclerView();
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
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ⭐ SỬA LỖI TẠI ĐÂY: Bỏ new ArrayList<>() ra khỏi constructor
        cartAdapter = new CartAdapter(new CartAdapter.CartItemListener() {
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

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems == null || cartItems.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                checkoutGroup.setVisibility(View.GONE);
                emptyCartLayout.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                checkoutGroup.setVisibility(View.VISIBLE);
                emptyCartLayout.setVisibility(View.GONE);
                cartAdapter.submitList(cartItems);
            }
        });

        cartViewModel.getSubtotal().observe(this, subtotal -> {
            if (subtotal != null) {
                updateTotalsUI(subtotal);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.checkout_button).setOnClickListener(v -> { /* Implement checkout logic */ });
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        continueShoppingButton.setOnClickListener(v -> finish());

        findViewById(R.id.checkout_button).setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void updateTotalsUI(double subTotal) {
        double shipping = (subTotal > 0) ? 30000 : 0.00;
        double bagTotal = subTotal + shipping;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        subTotalValue.setText(formatter.format(subTotal));
        shippingValue.setText(formatter.format(shipping));
        bagTotalValue.setText(formatter.format(bagTotal));
    }
}