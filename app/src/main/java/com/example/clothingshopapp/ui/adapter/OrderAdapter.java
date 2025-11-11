package com.example.clothingshopapp.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.data.model.OrderItem;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class OrderAdapter extends ListAdapter<Order, OrderAdapter.OrderViewHolder> {

    private static final String TAG = "OrderAdapter";
    private final OrderInteractionListener listener;
    private final boolean isHistoryTab;
    // Đặt 1 phút để test, sau này đổi lại 15
    private static final long PAYMENT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);

    // Interface để giao tiếp ngược lại Activity
    public interface OrderInteractionListener {
        void onTrackOrderClicked(Order order);
        void onCancelOrderClicked(Order order);
        void onPayNowClicked(Order order);
        void onPaymentExpired(Order order);
        void onOrderClicked(Order order); // Để xem chi tiết
    }

    public OrderAdapter(OrderInteractionListener listener, boolean isHistoryTab) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.isHistoryTab = isHistoryTab; // true = tab History, false = tab Ongoing
    }

    // DiffUtil để ListAdapter tự động nhận diện thay đổi
    private static final DiffUtil.ItemCallback<Order> DIFF_CALLBACK = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            if (oldItem.getOrderId() == null || newItem.getOrderId() == null) return false;
            return oldItem.getOrderId().equals(newItem.getOrderId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            if (oldItem.getStatus() == null || newItem.getStatus() == null) return false;
            return oldItem.getStatus().equals(newItem.getStatus()) &&
                    getItemCount(oldItem) == getItemCount(newItem); // Kiểm tra cả số lượng
        }

        private int getItemCount(Order order) {
            if (order.getItems() == null) return 0;
            int count = 0;
            for (OrderItem item : order.getItems()) {
                count += item.getQuantity();
            }
            return count;
        }
    };

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = getItem(position);
        if (order != null) {
            holder.bind(order, listener, isHistoryTab, PAYMENT_TIMEOUT_MS);
        } else {
            Log.w(TAG, "Attempting to bind null Order at position: " + position);
        }
    }

    // Hủy timer cũ khi ViewHolder được tái sử dụng
    @Override
    public void onViewRecycled(@NonNull OrderViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.currentTimer != null) {
            holder.currentTimer.cancel();
            holder.currentTimer = null;
        }
    }

    // Lớp ViewHolder
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, orderStatusText, orderDateText, orderItemCountText, orderTotalAmountText, orderTimerText;
        ImageView orderImageView;
        LinearLayout orderActionButtons;
        MaterialButton trackOrderButton, cancelOrderButton, payNowButton;
        Context context;
        CountDownTimer currentTimer; // Biến lưu timer

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderStatusText = itemView.findViewById(R.id.orderStatusText);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            orderItemCountText = itemView.findViewById(R.id.orderItemCountText);
            orderTotalAmountText = itemView.findViewById(R.id.orderTotalAmountText);
            orderImageView = itemView.findViewById(R.id.orderImageView);
            orderActionButtons = itemView.findViewById(R.id.orderActionButtons);
            trackOrderButton = itemView.findViewById(R.id.trackOrderButton);
            cancelOrderButton = itemView.findViewById(R.id.cancelOrderButton);
            payNowButton = itemView.findViewById(R.id.payNowButton);
            orderTimerText = itemView.findViewById(R.id.orderTimerText);
        }

        public void bind(final Order order, final OrderInteractionListener listener, boolean isHistory, long paymentTimeoutMs) {

            if (currentTimer != null) {
                currentTimer.cancel();
                currentTimer = null;
            }

            // Gán dữ liệu (đã sửa lỗi đếm item)
            orderIdText.setText("Order #" + (order.getOrderId() != null && order.getOrderId().length() > 8 ?
                    order.getOrderId().substring(order.getOrderId().length() - 8) : order.getOrderId()));
            orderStatusText.setText(order.getStatus() != null ? order.getStatus() : "Unknown");
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            orderDateText.setText("Placed on: " + sdf.format(new Date(order.getTimestamp())));

            int totalItemCount = 0;
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    totalItemCount += item.getQuantity();
                }
            }
            orderItemCountText.setText(totalItemCount + (totalItemCount > 1 ? " Items" : " Item"));

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            orderTotalAmountText.setText(formatter.format(order.getTotalAmount()));

            // Load hình ảnh
            if (order.getItems() != null && !order.getItems().isEmpty() && order.getItems().get(0) != null) {
                OrderItem firstItem = order.getItems().get(0);
                if (firstItem.getImageUrl() != null) {
                    Glide.with(context).load(firstItem.getImageUrl()).placeholder(R.color.gray_icon).error(R.drawable.ic_error_placeholder).into(orderImageView);
                } else { orderImageView.setImageResource(R.drawable.ic_error_placeholder); }
            } else { orderImageView.setImageResource(R.drawable.ic_error_placeholder); }

            // Reset tất cả các nút
            orderActionButtons.setVisibility(View.GONE);
            payNowButton.setVisibility(View.GONE);
            orderTimerText.setVisibility(View.GONE);

            String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "unknown";

            // Đổi màu Status
            int statusColor;
            switch (status) {
                case "delivered": case "completed":
                    statusColor = ContextCompat.getColor(context, R.color.green);
                    break;
                case "cancelled": case "paymentfailed":
                    statusColor = ContextCompat.getColor(context, R.color.coral);
                    break;
                case "pendingpayment":
                    statusColor = ContextCompat.getColor(context, R.color.blue);
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.dark_navy);
                    break;
            }
            orderStatusText.setTextColor(statusColor);

            // Chỉ hiện nút nếu là tab "Ongoing"
            if (!isHistory) {
                if (status.equals("pendingpayment")) {
                    // LOGIC HIỂN THỊ TIMER
                    long expirationTime = order.getTimestamp() + paymentTimeoutMs;
                    long remainingTime = expirationTime - System.currentTimeMillis();

                    if (remainingTime > 0) {
                        payNowButton.setVisibility(View.VISIBLE);
                        payNowButton.setEnabled(true);
                        payNowButton.setText("Thanh toán ngay");
                        orderTimerText.setVisibility(View.VISIBLE);
                        payNowButton.setOnClickListener(v -> listener.onPayNowClicked(order));

                        currentTimer = new CountDownTimer(remainingTime, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                String time = String.format(Locale.US, "%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                                );
                                orderTimerText.setText("Thời gian còn lại: " + time);
                            }

                            // ⭐ DÒNG NÀY LÀ NƠI GÂY LỖI, ĐÃ SỬA LẠI ⭐
                            @Override
                            public void onFinish() {
                                // Khi hết giờ
                                orderTimerText.setText("Đã hết hạn");
                                payNowButton.setEnabled(false);
                                payNowButton.setText("Đã hết hạn");
                                listener.onPaymentExpired(order);
                            }
                        }.start();
                    } else {
                        // Đã hết hạn
                        orderTimerText.setVisibility(View.VISIBLE);
                        orderTimerText.setText("Đã hết hạn");
                        payNowButton.setVisibility(View.VISIBLE);
                        payNowButton.setEnabled(false);
                        payNowButton.setText("Đã hết hạn");
                        listener.onPaymentExpired(order);
                    }

                } else if (status.equals("pending") || status.equals("processing") || status.equals("shipped")) {
                    // Các trạng thái Ongoing khác (COD,...)
                    orderActionButtons.setVisibility(View.VISIBLE);
                    trackOrderButton.setOnClickListener(v -> listener.onTrackOrderClicked(order));
                    cancelOrderButton.setOnClickListener(v -> listener.onCancelOrderClicked(order));
                }
            }

            // Listener để nhấn vào xem chi tiết (cho cả 2 tab)
            itemView.setOnClickListener(v -> {
                listener.onOrderClicked(order);
            });
        }
    }
}