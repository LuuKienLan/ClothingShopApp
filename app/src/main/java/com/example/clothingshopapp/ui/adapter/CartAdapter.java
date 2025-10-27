package com.example.clothingshopapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.NumberFormat;
import java.util.Locale;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private final CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(CartItemListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            // Key của Firebase là định danh duy nhất
            if(oldItem.getFirebaseKey() != null && newItem.getFirebaseKey() != null) {
                return oldItem.getFirebaseKey().equals(newItem.getFirebaseKey());
            }
            // Fallback nếu không có key
            return oldItem.getProduct().getProductId().equals(newItem.getProduct().getProductId()) &&
                    oldItem.getVariant().getColor().equals(newItem.getVariant().getColor()) &&
                    oldItem.getSize().equals(newItem.getSize());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = getItem(position);
        holder.bind(item, listener);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productAttributes, productPrice, quantityText;
        Button buttonMinus, buttonPlus;
        ImageView deleteIcon;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productAttributes = itemView.findViewById(R.id.product_attributes); // Gán vào ID mới
            productPrice = itemView.findViewById(R.id.product_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            buttonMinus = itemView.findViewById(R.id.button_minus);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }

        public void bind(final CartItem item, final CartItemListener listener) {
            productName.setText(item.getProduct().getName());

            // ⭐ CẬP NHẬT TEXT CHO CẢ MÀU VÀ SIZE ⭐
            String attributes = "Màu sắc: " + item.getVariant().getColor() + " / Size: " + item.getSize();
            productAttributes.setText(attributes);

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPrice.setText(formatter.format(item.getProduct().getBasePrice()));

            quantityText.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getVariant().getImageUrl())
                    .placeholder(R.color.gray_icon)
                    .into(productImage);

            // Listeners
            buttonPlus.setOnClickListener(v -> listener.onQuantityChanged(item, item.getQuantity() + 1));
            buttonMinus.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                } else {
                    showRemoveConfirmationDialog(item, listener);
                }
            });
            deleteIcon.setOnClickListener(v -> showRemoveConfirmationDialog(item, listener));
        }
        private void showRemoveConfirmationDialog(final CartItem item, final CartItemListener listener) {
            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có chắc chắn muốn bỏ sản phẩm này khỏi giỏ hàng không?")
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        // Người dùng chọn "Hủy", không làm gì cả
                        dialog.dismiss();
                    })
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Người dùng chọn "Xóa", gọi đến interface để xóa item
                        listener.onItemRemoved(item);
                    })
                    .show();
        }
    }
}