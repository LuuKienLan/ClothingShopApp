package com.example.clothingshopapp.ui.orders;

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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.ui.adapter.OrderAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private FirebaseUser currentUser;
    private boolean isOngoingTabSelected = true;

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
        // Không gọi loadOrders() ở đây
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders(); // Tải lại dữ liệu
        selectTabUIOnly(isOngoingTabSelected); // Chỉ cập nhật UI tab, không ẩn/hiện list vội
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
        ongoingAdapter = new OrderAdapter(this, true);
        historyAdapter = new OrderAdapter(this, false);
        ongoingRecyclerView.setAdapter(ongoingAdapter);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        // Khi nhấn tab, chỉ cập nhật UI tab và trạng thái, sau đó gọi loadOrders
        ongoingTab.setOnClickListener(v -> {
            isOngoingTabSelected = true;
            selectTabUIOnly(true);
            loadOrders(); // Tải lại và hiển thị đúng list
        });
        historyTab.setOnClickListener(v -> {
            isOngoingTabSelected = false;
            selectTabUIOnly(false);
            loadOrders(); // Tải lại và hiển thị đúng list
        });
    }

    // Hàm mới: Chỉ cập nhật giao diện Tab (màu sắc, gạch chân)
    private void selectTabUIOnly(boolean isOngoing) {
        if (isOngoing) {
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTypeface(null, android.graphics.Typeface.BOLD);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            historyText.setTypeface(null, android.graphics.Typeface.NORMAL);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        } else {
            historyText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTypeface(null, android.graphics.Typeface.BOLD);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            ongoingText.setTypeface(null, android.graphics.Typeface.NORMAL);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        }
    }


    private void loadOrders() {
        ordersProgressBar.setVisibility(View.VISIBLE);
        noOrdersText.setVisibility(View.GONE);
        ongoingRecyclerView.setVisibility(View.GONE);
        historyRecyclerView.setVisibility(View.GONE);

        Query userOrdersQuery = ordersRef.orderByChild("userId").equalTo(currentUser.getUid());

        // ⭐ SỬ DỤNG addListenerForSingleValueEvent ⭐
        userOrdersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Order> allOrders = new ArrayList<>();

                Log.d(TAG, "Firebase returned " + dataSnapshot.getChildrenCount() + " potential orders for user " + currentUser.getUid());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null && order.getOrderId() != null) {
                        allOrders.add(order);
                    } else {
                        Log.w(TAG, "Skipping invalid order data: " + snapshot.getKey());
                    }
                }
                Collections.sort(allOrders, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                processAndDisplayOrders(allOrders); // Xử lý và hiển thị
                ordersProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ordersProgressBar.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE); // Hiện text lỗi nếu không tải được
                Log.e(TAG, "Failed to load orders.", databaseError.toException());
                Toast.makeText(MyOrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ⭐ SỬA LẠI HÀM NÀY ⭐
    private void processAndDisplayOrders(List<Order> allOrders) {
        List<Order> ongoingList = new ArrayList<>();
        List<Order> historyList = new ArrayList<>();

        for (Order order : allOrders) {
            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";
            if (status.equals("delivered") || status.equals("completed") || status.equals("cancelled")) {
                historyList.add(order);
            } else {
                ongoingList.add(order);
            }
        }

        // Cập nhật dữ liệu cho cả hai adapter
        ongoingAdapter.submitList(ongoingList);
        historyAdapter.submitList(historyList);

        // Quyết định hiển thị RecyclerView nào và text "No orders"
        if (isOngoingTabSelected) {
            if (ongoingList.isEmpty()) {
                ongoingRecyclerView.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE);
            } else {
                ongoingRecyclerView.setVisibility(View.VISIBLE);
                noOrdersText.setVisibility(View.GONE);
            }
            historyRecyclerView.setVisibility(View.GONE); // Luôn ẩn history
        } else { // History tab is selected
            if (historyList.isEmpty()) {
                historyRecyclerView.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE);
            } else {
                historyRecyclerView.setVisibility(View.VISIBLE);
                noOrdersText.setVisibility(View.GONE);
            }
            ongoingRecyclerView.setVisibility(View.GONE); // Luôn ẩn ongoing
        }
    }

    @Override
    public void onTrackOrderClicked(Order order) {
        Toast.makeText(this, "Tracking order: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelOrderClicked(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel order #" + (order.getOrderId() != null && order.getOrderId().length() > 8 ?
                        order.getOrderId().substring(order.getOrderId().length()-8) : order.getOrderId()) + "?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Cập nhật status trên Firebase
                    ordersRef.child(order.getOrderId()).child("status").setValue("Cancelled")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MyOrdersActivity.this, "Order Cancelled", Toast.LENGTH_SHORT).show();
                                // Tải lại danh sách sau khi hủy thành công để cập nhật UI
                                loadOrders();
                            })
                            .addOnFailureListener(e -> Toast.makeText(MyOrdersActivity.this, "Failed to cancel order", Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    // Không cần onPause hay onDestroy nếu dùng addListenerForSingleValueEvent
}