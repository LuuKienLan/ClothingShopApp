package com.example.clothingshopapp.ui.orders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.data.model.OrderItem;
import com.example.clothingshopapp.data.model.Product;
import com.example.clothingshopapp.data.model.Variant;
import com.example.clothingshopapp.ui.adapter.CartAdapter;
import com.example.clothingshopapp.ui.cart.CheckoutActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private RecyclerView orderItemsRecyclerView;
    private CartAdapter cartAdapter;
    private TextView shippingAddressText, subTotalValue, shippingValue, totalAmountValue;
    private Button confirmOrderButton;
    private LinearLayout bottomActionBar;
    private MaterialToolbar toolbar;

    private LinearLayout orderInfoLayout;
    private TextView orderIdText, orderDateText, orderStatusText;
    private TextView shippingPhoneText;

    private List<CartItem> itemsForCheckout = new ArrayList<>();
    private boolean isBuyNowFlow = false;
    private Order viewedOrder = null;
    private double shippingFee = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        initViews();
        setupRecyclerView();

        try {
            // TRƯỜNG HỢP 1: XEM CHI TIẾT ĐƠN HÀNG
            if (getIntent().hasExtra("VIEW_ORDER")) {
                viewedOrder = (Order) getIntent().getSerializableExtra("VIEW_ORDER");

                // ⭐ SỬA 8: Dịch tiêu đề động
                toolbar.setTitle("Order Details");

                bottomActionBar.setVisibility(View.GONE);
                orderInfoLayout.setVisibility(View.VISIBLE);
                displayOrderDetails(viewedOrder);
            }
            // TRƯỜNG HỢP 2: XÁC NHẬN CHECKOUT
            else if (getIntent().hasExtra("CHECKOUT_ITEMS")) {
                itemsForCheckout = (List<CartItem>) getIntent().getSerializableExtra("CHECKOUT_ITEMS");
                isBuyNowFlow = getIntent().getBooleanExtra("IS_BUY_NOW", false);

                // ⭐ SỬA 9: Dịch tiêu đề động
                toolbar.setTitle("Confirm Order");

                bottomActionBar.setVisibility(View.VISIBLE);
                orderInfoLayout.setVisibility(View.GONE);
                displayCheckoutSummary();
            } else {
                Toast.makeText(this, "Error: No order data found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        confirmOrderButton.setOnClickListener(v -> proceedToPayment());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        shippingAddressText = findViewById(R.id.shippingAddressText);
        subTotalValue = findViewById(R.id.subTotalValue);
        shippingValue = findViewById(R.id.shippingValue);
        totalAmountValue = findViewById(R.id.totalAmountValue);
        confirmOrderButton = findViewById(R.id.confirmOrderButton);
        bottomActionBar = findViewById(R.id.bottomActionBar);

        orderInfoLayout = findViewById(R.id.orderInfoLayout);
        orderIdText = findViewById(R.id.orderIdText);
        orderDateText = findViewById(R.id.orderDateText);
        orderStatusText = findViewById(R.id.orderStatusText);
        shippingPhoneText = findViewById(R.id.shippingPhoneText);
    }

    private void setupRecyclerView() {
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (getIntent().hasExtra("VIEW_ORDER")) {
            cartAdapter = new CartAdapter(null, true);
        } else {
            cartAdapter = new CartAdapter(null, true);
        }

        orderItemsRecyclerView.setAdapter(cartAdapter);
    }

    // ⭐ SỬA 10: DỊCH CÁC NHÃN ĐỘNG
    private void displayOrderDetails(Order order) {
        if (order == null) return;

        // GÁN DỮ LIỆU ĐƠN HÀNG (Đã dịch)
        orderIdText.setText("Order ID: " + order.getOrderId());
        orderStatusText.setText("Status: " + order.getStatus());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(order.getTimestamp()));
        orderDateText.setText("Date placed: " + formattedDate);

        // (Code set màu giữ nguyên)
        String status = order.getStatus().toLowerCase();
        int statusColor;
        if (status.equals("delivered") || status.equals("completed")) {
            statusColor = ContextCompat.getColor(this, R.color.green);
        } else if (status.equals("cancelled") || status.equals("paymentfailed")) {
            statusColor = ContextCompat.getColor(this, R.color.coral);
        } else {
            statusColor = ContextCompat.getColor(this, R.color.blue);
        }
        orderStatusText.setTextColor(statusColor);


        // GÁN ĐỊA CHỈ VÀ SĐT (Đã có)
        shippingAddressText.setText(order.getShippingAddress());

        String phone = order.getPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            shippingPhoneText.setText(phone);
            shippingPhoneText.setVisibility(View.VISIBLE);
        } else {
            shippingPhoneText.setVisibility(View.GONE);
        }

        // (Code hiển thị sản phẩm giữ nguyên)
        List<CartItem> cartItems = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Product p = new Product();
                p.setProductId(item.getProductId());
                p.setName(item.getProductName());
                p.setBasePrice(item.getPrice());

                Variant v = new Variant(item.getColor(), item.getImageUrl());

                CartItem ci = new CartItem(p, v, item.getQuantity(), item.getSize());
                cartItems.add(ci);
            }
        }
        cartAdapter.submitList(cartItems);

        double fee = order.getShippingFee();
        double subtotal = order.getTotalAmount() - fee;
        updateTotalsUI(subtotal, fee);
    }

    private void displayCheckoutSummary() {
        shippingAddressText.setText("Address will be selected in the next step...");
        shippingPhoneText.setVisibility(View.GONE);
        cartAdapter.submitList(itemsForCheckout);
        updateTotalsUI(calculateSubtotal(itemsForCheckout), shippingFee);
    }

    private void proceedToPayment() {
        Intent intent = new Intent(OrderDetailActivity.this, CheckoutActivity.class);
        intent.putExtra("CHECKOUT_ITEMS", (ArrayList<CartItem>) itemsForCheckout);
        intent.putExtra("IS_BUY_NOW", isBuyNowFlow);
        startActivity(intent);
    }

    private double calculateSubtotal(List<CartItem> items) {
        double subtotal = 0;
        if (items != null) {
            for (CartItem item : items) {
                subtotal += item.getProduct().getBasePrice() * item.getQuantity();
            }
        }
        return subtotal;
    }

    private void updateTotalsUI(double subTotal, double fee) {
        double totalAmount = subTotal + fee;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        subTotalValue.setText(formatter.format(subTotal));
        shippingValue.setText(formatter.format(fee));
        totalAmountValue.setText(formatter.format(totalAmount));
    }
}