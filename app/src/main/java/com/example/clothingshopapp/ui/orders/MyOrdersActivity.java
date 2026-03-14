package com.example.clothingshopapp.ui.orders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.ui.adapter.OrderAdapter;
import com.example.clothingshopapp.ui.cart.VNPAYHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MyOrdersActivity extends AppCompatActivity implements OrderAdapter.OrderInteractionListener {

    private static final String TAG = "MyOrdersActivity";
    private ImageView backButton;
    private LinearLayout ongoingTab, historyTab;
    private TextView ongoingText, historyText, noOrdersText;
    private View ongoingIndicator, historyIndicator;
    private RecyclerView ongoingRecyclerView, historyRecyclerView;
    private ProgressBar ordersProgressBar;
    private OrderAdapter ongoingAdapter;
    private OrderAdapter historyAdapter;
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;
    private FirebaseUser currentUser;
    private boolean isOngoingTabSelected = true;
    private static final long PAYMENT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1); // 1 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view orders", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        initViews();
        setupRecyclerViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
        selectTabUIOnly(isOngoingTabSelected);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ordersListener != null && currentUser != null) {
            Query userOrdersQuery = ordersRef.orderByChild("userId").equalTo(currentUser.getUid());
            userOrdersQuery.removeEventListener(ordersListener);
        }
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        ongoingTab = findViewById(R.id.ongoingTab);
        historyTab = findViewById(R.id.historyTab);
        ongoingText = findViewById(R.id.ongoingText);
        historyText = findViewById(R.id.historyText);
        ongoingIndicator = findViewById(R.id.ongoingIndicator);
        historyIndicator = findViewById(R.id.historyIndicator);
        ongoingRecyclerView = findViewById(R.id.ongoingRecyclerView);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        ordersProgressBar = findViewById(R.id.ordersProgressBar);
        noOrdersText = findViewById(R.id.noOrdersText);
    }

    private void setupRecyclerViews() {
        ongoingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ongoingAdapter = new OrderAdapter(this, false);
        historyAdapter = new OrderAdapter(this, true);
        ongoingRecyclerView.setAdapter(ongoingAdapter);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        ongoingTab.setOnClickListener(v -> {
            isOngoingTabSelected = true;
            selectTabUIOnly(true);
            updateNoOrdersTextVisibility();
        });
        historyTab.setOnClickListener(v -> {
            isOngoingTabSelected = false;
            selectTabUIOnly(false);
            updateNoOrdersTextVisibility();
        });
    }

    private void selectTabUIOnly(boolean isOngoing) {
        if (isOngoing) {
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTypeface(null, android.graphics.Typeface.BOLD);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            historyText.setTypeface(null, android.graphics.Typeface.NORMAL);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            ongoingRecyclerView.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            historyText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTypeface(null, android.graphics.Typeface.BOLD);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            ongoingText.setTypeface(null, android.graphics.Typeface.NORMAL);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            ongoingRecyclerView.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadOrders() {
        ordersProgressBar.setVisibility(View.VISIBLE);
        noOrdersText.setVisibility(View.GONE);
        Query userOrdersQuery = ordersRef.orderByChild("userId").equalTo(currentUser.getUid());
        if (ordersListener != null) {
            userOrdersQuery.removeEventListener(ordersListener);
        }
        ordersListener = userOrdersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> allOrders = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null && order.getOrderId() != null) {
                        allOrders.add(order);
                    } else {
                        Log.w(TAG, "Skipping invalid order data: " + snapshot.getKey());
                    }
                }
                Collections.sort(allOrders, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                processAndDisplayOrders(allOrders);
                ordersProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ordersProgressBar.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE);
                Log.e(TAG, "Failed to load orders.", databaseError.toException());
                Toast.makeText(MyOrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processAndDisplayOrders(List<Order> allOrders) {
        List<Order> ongoingList = new ArrayList<>();
        List<Order> historyList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Order order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";

            // "Janitor" Logic: Chỉ check những cái còn "PendingPayment"
            if (status.equals("pendingpayment")) {
                long orderTime = order.getTimestamp();
                if ((currentTime - orderTime) > PAYMENT_TIMEOUT_MS) {
                    status = "paymentfailed";
                    // Chúng ta vẫn update, nhưng việc này sẽ bị "Banker" (VNPAY)
                    // đè lên nếu thanh toán thành công (vì Banker update timestamp)
                    updateOrderStatusInFirebase(order.getOrderId(), "PaymentFailed");
                }
            }

            if (status.equals("delivered") || status.equals("completed") || status.equals("cancelled") || status.equals("paymentfailed")) {
                historyList.add(order);
            } else {
                // (Bao gồm "Pending", "Processing", "Shipped", "PendingPayment" (chưa hết hạn))
                ongoingList.add(order);
            }
        }
        ongoingAdapter.submitList(ongoingList);
        historyAdapter.submitList(historyList);
        updateNoOrdersTextVisibility();
    }

    private void updateNoOrdersTextVisibility() {
        boolean ongoingIsEmpty = ongoingAdapter.getCurrentList().isEmpty();
        boolean historyIsEmpty = historyAdapter.getCurrentList().isEmpty();
        if (isOngoingTabSelected) {
            ongoingRecyclerView.setVisibility(ongoingIsEmpty ? View.GONE : View.VISIBLE);
            noOrdersText.setVisibility(ongoingIsEmpty ? View.VISIBLE : View.GONE);
        } else {
            historyRecyclerView.setVisibility(historyIsEmpty ? View.GONE : View.VISIBLE);
            noOrdersText.setVisibility(historyIsEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void updateOrderStatusInFirebase(String orderId, String newStatus) {
        if (orderId != null) {
            ordersRef.child(orderId).child("status").setValue(newStatus)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Order " + orderId + " auto-updated to " + newStatus))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to auto-update status for " + orderId));
        }
    }

    @Override
    public void onTrackOrderClicked(Order order) {
        Intent intent = new Intent(MyOrdersActivity.this, TrackOrderActivity.class);
        intent.putExtra("ORDER_ID", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onCancelOrderClicked(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel order #" + (order.getOrderId() != null && order.getOrderId().length() > 8 ?
                        order.getOrderId().substring(order.getOrderId().length()-8) : order.getOrderId()) + "?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    ordersRef.child(order.getOrderId()).child("status").setValue("Cancelled")
                            .addOnSuccessListener(aVoid -> Toast.makeText(MyOrdersActivity.this, "Order Cancelled", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MyOrdersActivity.this, "Failed to cancel order", Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    @Override
    public void onPayNowClicked(Order order) {
        long amountToPay = (long) order.getTotalAmount();
        String orderId = order.getOrderId();
        String paymentUrl = VNPAYHelper.createPaymentUrl(this, orderId, amountToPay);
        if (paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi khi tạo link thanh toán.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đang chuyển đến VNPAY...", Toast.LENGTH_SHORT).show();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(paymentUrl));
    }

    @Override
    public void onPaymentExpired(Order order) {
        Toast.makeText(this, "Đơn hàng #" + order.getOrderId().substring(order.getOrderId().length()-8) + " đã hết hạn thanh toán.", Toast.LENGTH_SHORT).show();

        String orderId = order.getOrderId();
        if (orderId == null) return;

        // "Giết Zombie":
        // Trước khi tự ý đổi status, chúng ta phải "check" lại Firebase
        // xem status *hiện tại* là gì.
        ordersRef.child(orderId).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentStatus = snapshot.getValue(String.class);

                // Chỉ đổi thành "PaymentFailed" NẾU nó VẪN CÒN là "PendingPayment"
                if (currentStatus != null && currentStatus.equalsIgnoreCase("pendingpayment")) {
                    Log.d(TAG, "onPaymentExpired: Xác nhận đơn " + orderId + " đã hết hạn. Đang cập nhật...");
                    updateOrderStatusInFirebase(orderId, "PaymentFailed");
                } else {
                    // Nếu status là "Pending" (tức là "Banker" đã chạy)
                    // -> KHÔNG LÀM GÌ CẢ.
                    Log.d(TAG, "onPaymentExpired: Đơn " + orderId + " đã được thanh toán (Status=" + currentStatus + "). Zombie Timer bị hủy.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onPaymentExpired: Không thể check status của Zombie Timer.", error.toException());
            }
        });
    }
    // ⭐⭐⭐ KẾT THÚC SỬA ⭐⭐⭐

    @Override
    public void onOrderClicked(Order order) {
        Intent intent = new Intent(MyOrdersActivity.this, OrderDetailActivity.class);
        intent.putExtra("VIEW_ORDER", (Serializable) order);
        startActivity(intent);
    }
}