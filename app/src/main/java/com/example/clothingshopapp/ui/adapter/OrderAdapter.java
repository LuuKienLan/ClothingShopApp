package com.example.clothingshopapp.ui.adapter;

import android.content.Context;
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

public class OrderAdapter extends ListAdapter<Order, OrderAdapter.OrderViewHolder> {

    private final OrderInteractionListener listener;
    private final boolean showActionButtons;

    public interface OrderInteractionListener {
        void onTrackOrderClicked(Order order);
        void onCancelOrderClicked(Order order);
    }

    public OrderAdapter(OrderInteractionListener listener, boolean showActionButtons) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.showActionButtons = showActionButtons;
    }

    private static final DiffUtil.ItemCallback<Order> DIFF_CALLBACK = new DiffUtil.ItemCallback<Order>() {
        @Override
        public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getOrderId().equals(newItem.getOrderId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
            return oldItem.getStatus().equals(newItem.getStatus());
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
        holder.bind(order, listener, showActionButtons);
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, orderStatusText, orderDateText, orderItemCountText, orderTotalAmountText;
        ImageView orderImageView;
        LinearLayout orderActionButtons;
        MaterialButton trackOrderButton, cancelOrderButton;
        Context context;

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
        }

        public void bind(final Order order, final OrderInteractionListener listener, boolean showButtons) {
            // Display partial Order ID for brevity
            orderIdText.setText("Order #" + (order.getOrderId() != null && order.getOrderId().length() > 8 ?
                    order.getOrderId().substring(order.getOrderId().length() - 8) : order.getOrderId()));
            orderStatusText.setText(order.getStatus());

            // Format timestamp to date
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            orderDateText.setText("Placed on: " + sdf.format(new Date(order.getTimestamp())));

            // Item count
            int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;
            orderItemCountText.setText(itemCount + (itemCount > 1 ? " Items" : " Item"));

            // Total amount formatted
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            orderTotalAmountText.setText(formatter.format(order.getTotalAmount()));

            // Load image of the first item
            if (order.getItems() != null && !order.getItems().isEmpty() && order.getItems().get(0) != null) {
                OrderItem firstItem = order.getItems().get(0);
                Glide.with(context)
                        .load(firstItem.getImageUrl())
                        .placeholder(R.color.gray_icon) // Use your placeholder color
                        .error(R.drawable.ic_error_placeholder) // Add an error placeholder drawable
                        .into(orderImageView);
            } else {
                orderImageView.setImageResource(R.drawable.ic_error_placeholder); // Default image if no items
            }

            // Set status text color based on status
            int statusColor;
            switch (order.getStatus().toLowerCase()) {
                case "delivered": case "completed":
                    statusColor = ContextCompat.getColor(context, R.color.green); // Define R.color.green
                    break;
                case "cancelled":
                    statusColor = ContextCompat.getColor(context, R.color.coral);
                    break;
                case "pending": case "processing": case "shipped": default:
                    statusColor = ContextCompat.getColor(context, R.color.blue); // Define R.color.blue
                    break;
            }
            orderStatusText.setTextColor(statusColor);

            // Show/Hide action buttons based on status and the 'showButtons' flag
            if (showButtons && ("pending".equalsIgnoreCase(order.getStatus()) || "processing".equalsIgnoreCase(order.getStatus()))) {
                orderActionButtons.setVisibility(View.VISIBLE);
                trackOrderButton.setOnClickListener(v -> listener.onTrackOrderClicked(order));
                cancelOrderButton.setOnClickListener(v -> listener.onCancelOrderClicked(order));
            } else {
                orderActionButtons.setVisibility(View.GONE);
            }
        }
    }
}