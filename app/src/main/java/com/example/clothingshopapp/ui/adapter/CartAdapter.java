package com.example.clothingshopapp.ui.adapter;

import android.content.Context;
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
    private final boolean isReadOnly; // ⭐ BIẾN ĐỂ KIỂM SOÁT

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    // Constructor cũ (dùng cho CartActivity)
    public CartAdapter(CartItemListener listener) {
        this(listener, false); // Mặc định là không "chỉ đọc"
    }

    // ⭐ CONSTRUCTOR MỚI (dùng cho OrderDetailActivity)
    public CartAdapter(CartItemListener listener, boolean isReadOnly) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.isReadOnly = isReadOnly; // Gán giá trị
    }

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            if (oldItem.getFirebaseKey() != null && newItem.getFirebaseKey() != null) {
                return oldItem.getFirebaseKey().equals(newItem.getFirebaseKey());
            }
            // Fallback cho trường hợp key null (ví dụ: màn hình checkout "Buy Now")
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
        // ⭐ Truyền isReadOnly xuống ViewHolder
        return new CartViewHolder(view, isReadOnly);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = getItem(position);
        if (item != null) {
            holder.bind(item, listener);
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productAttributes, productPrice, quantityText;
        Button buttonMinus, buttonPlus;
        ImageView deleteIcon;
        private final boolean isReadOnlyView; // ⭐ Biến lưu trạng thái

        public CartViewHolder(@NonNull View itemView, boolean isReadOnly) { // ⭐ Sửa constructor
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productAttributes = itemView.findViewById(R.id.product_attributes);
            productPrice = itemView.findViewById(R.id.product_price);
            quantityText = itemView.findViewById(R.id.quantity_text);
            buttonMinus = itemView.findViewById(R.id.button_minus);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            deleteIcon = itemView.findViewById(R.id.delete_icon);

            this.isReadOnlyView = isReadOnly; // ⭐ Lưu lại
        }

        public void bind(final CartItem item, final CartItemListener listener) {
            productName.setText(item.getProduct().getName());
            String attributes = "Màu sắc: " + item.getVariant().getColor() + " / Size: " + item.getSize();
            productAttributes.setText(attributes);

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            productPrice.setText(formatter.format(item.getProduct().getBasePrice()));

            quantityText.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getVariant().getFirstImageUrl())
                    .placeholder(R.color.gray_icon)
                    .error(R.drawable.ic_error_placeholder)
                    .into(productImage);

            // ⭐ LOGIC ẨN/HIỆN QUAN TRỌNG
            if (isReadOnlyView) {
                // Chế độ "Chỉ đọc": Ẩn hết các nút
                buttonMinus.setVisibility(View.GONE);
                buttonPlus.setVisibility(View.GONE);
                deleteIcon.setVisibility(View.GONE);

                // Hiển thị số lượng rõ hơn
                quantityText.setText("SL: " + item.getQuantity());

            } else {
                // Chế độ "Giỏ hàng": Hiện các nút và gán listener
                buttonMinus.setVisibility(View.VISIBLE);
                buttonPlus.setVisibility(View.VISIBLE);
                deleteIcon.setVisibility(View.VISIBLE);

                // Listener chỉ được gán khi không phải read-only
                // (Kiểm tra listener != null để tránh lỗi khi truyền null từ OrderDetailActivity)
                if (listener != null) {
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
            }
        }

        private void showRemoveConfirmationDialog(final CartItem item, final CartItemListener listener) {
            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có chắc chắn muốn bỏ sản phẩm này?")
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (listener != null) {
                            listener.onItemRemoved(item);
                        }
                    })
                    .show();
        }
    }
}