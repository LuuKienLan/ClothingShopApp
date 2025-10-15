package com.example.clothingshopapp.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.example.clothingshopapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageView backButton, menuButton;
    private LinearLayout ongoingTab, historyTab;
    private TextView ongoingText, historyText;
    private View ongoingIndicator, historyIndicator;
    private NestedScrollView ongoingContainer, historyContainer;
    private LinearLayout historyOrdersList;

    private MaterialButton trackOrder1, trackOrder2, trackOrder3;
    private MaterialButton cancelOrder1, cancelOrder2, cancelOrder3;

    private boolean isOngoingSelected = true;

    // Danh sách đơn ongoing đang giả lập
    private List<Order> ongoingOrders = new ArrayList<>();
    // Danh sách đơn đã hủy
    private List<Order> cancelledOrders = new ArrayList<>();
    // Danh sách đơn thành công (tab History)
    private List<Order> successOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        prepareFakeData();
        setupListeners();
        updateOngoingOrdersUI();
        updateHistoryOrdersUI();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        ongoingTab = findViewById(R.id.ongoingTab);
        historyTab = findViewById(R.id.historyTab);
        ongoingText = findViewById(R.id.ongoingText);
        historyText = findViewById(R.id.historyText);
        ongoingIndicator = findViewById(R.id.ongoingIndicator);
        historyIndicator = findViewById(R.id.historyIndicator);

        ongoingContainer = findViewById(R.id.ongoingContainer);
        historyContainer = findViewById(R.id.historyContainer);
        historyOrdersList = findViewById(R.id.historyOrdersList);

        trackOrder1 = findViewById(R.id.trackOrder1);
        trackOrder2 = findViewById(R.id.trackOrder2);
        trackOrder3 = findViewById(R.id.trackOrder3);

        cancelOrder1 = findViewById(R.id.cancelOrder1);
        cancelOrder2 = findViewById(R.id.cancelOrder2);
        cancelOrder3 = findViewById(R.id.cancelOrder3);
    }

    private void prepareFakeData() {
        // Đơn ongoing
        ongoingOrders.clear();
        ongoingOrders.add(new Order("T-shirt", "#162432", "$35.25"));
        ongoingOrders.add(new Order("Jacket - Nike", "#242432", "$40.15"));
        ongoingOrders.add(new Order("Pant", "#240112", "$10.20"));

        // Đơn success (lịch sử đã thành công)
        successOrders.clear();
        successOrders.add(new Order("Cap", "#100001", "$15.00"));
        successOrders.add(new Order("Socks", "#100002", "$5.50"));

        // Đơn cancelled bắt đầu rỗng
        cancelledOrders.clear();
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        menuButton.setOnClickListener(v ->
                Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show()
        );

        ongoingTab.setOnClickListener(v -> selectTab(true));
        historyTab.setOnClickListener(v -> selectTab(false));

        trackOrder1.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #162432", Toast.LENGTH_SHORT).show()
        );
        trackOrder2.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #242432", Toast.LENGTH_SHORT).show()
        );
        trackOrder3.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #240112", Toast.LENGTH_SHORT).show()
        );

        cancelOrder1.setOnClickListener(v -> showCancelDialog("T-shirt", "#162432"));
        cancelOrder2.setOnClickListener(v -> showCancelDialog("Jacket - Nike", "#242432"));
        cancelOrder3.setOnClickListener(v -> showCancelDialog("Pant", "#240112"));
    }

    private void selectTab(boolean isOngoing) {
        isOngoingSelected = isOngoing;

        if (isOngoing) {
            // Ongoing selected
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTypeface(null, android.graphics.Typeface.BOLD);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));

            historyText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            historyText.setTypeface(null, android.graphics.Typeface.NORMAL);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            ongoingContainer.setVisibility(View.VISIBLE);
            historyContainer.setVisibility(View.GONE);

        } else {
            // History selected
            historyText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTypeface(null, android.graphics.Typeface.BOLD);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));

            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            ongoingText.setTypeface(null, android.graphics.Typeface.NORMAL);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            ongoingContainer.setVisibility(View.GONE);
            historyContainer.setVisibility(View.VISIBLE);

            updateHistoryOrdersUI();
        }
    }

    private void showCancelDialog(String productName, String orderNumber) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng " + orderNumber + " - " + productName + "?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    // Xử lý hủy đơn
                    boolean removed = removeOrderFromOngoing(orderNumber);
                    if (removed) {
                        cancelledOrders.add(new Order(productName, orderNumber, ""));
                        Toast.makeText(this,
                                "Đã hủy đơn hàng " + orderNumber,
                                Toast.LENGTH_SHORT).show();
                        updateOngoingOrdersUI();
                        if (!isOngoingSelected) {
                            updateHistoryOrdersUI();
                        }
                    } else {
                        Toast.makeText(this,
                                "Không tìm thấy đơn hàng để hủy",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean removeOrderFromOngoing(String orderNumber) {
        Iterator<Order> iter = ongoingOrders.iterator();
        while (iter.hasNext()) {
            Order o = iter.next();
            if (o.orderNumber.equals(orderNumber)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    private void updateOngoingOrdersUI() {
        // Cập nhật số liệu trên các nút và giao diện ongoing nếu cần
        // Do bạn dùng 3 đơn cố định với nút track/cancel, ở đây cập nhật text và trạng thái theo danh sách ongoingOrders

        // Ví dụ đơn giản cập nhật tên, mã cho 3 nút:
        if (ongoingOrders.size() > 0) {
            trackOrder1.setText("Track Order");
            cancelOrder1.setText("Cancel");
        } else {
            trackOrder1.setText("No order");
            cancelOrder1.setText("No cancel");
        }
        // Tương tự cho trackOrder2/cancelOrder2, trackOrder3/cancelOrder3
        // Với code mẫu, bạn có thể thêm nếu muốn map từng đơn đến nút ở đây.
    }

    private void updateHistoryOrdersUI() {
        historyOrdersList.removeAllViews();

        addSectionTitle("Đơn đã hoàn thành");
        for (Order o : successOrders) {
            historyOrdersList.addView(createOrderView(o, false));
        }

        addSectionTitle("Đơn đã hủy");
        for (Order o : cancelledOrders) {
            historyOrdersList.addView(createOrderView(o, true));
        }
    }

    private void addSectionTitle(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(16f);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.dark_navy));
        tv.setPadding(0, 10, 0, 10);
        historyOrdersList.addView(tv);
    }

    private View createOrderView(Order order, boolean isCancelled) {
        TextView tv = new TextView(this);
        String text = order.name + " (" + order.orderNumber + ")";
        if (isCancelled) {
            text += " - [Đã hủy]";
            tv.setTextColor(ContextCompat.getColor(this, R.color.coral));
        } else {
            text += " - [Thành công]";
            tv.setTextColor(ContextCompat.getColor(this, R.color.dark_navy));
        }
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setPadding(10, 10, 10, 10);
        return tv;
    }

    private static class Order {
        String name;
        String orderNumber;
        String price;

        Order(String name, String orderNumber, String price) {
            this.name = name;
            this.orderNumber = orderNumber;
            this.price = price;
        }
    }
}
