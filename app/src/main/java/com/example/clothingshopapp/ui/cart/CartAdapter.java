// File: ui/cart/CartAdapter.java
package com.example.clothingshopapp.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private final CartItemListener listener;

    // Interface để báo hành động của user cho Activity/ViewModel
    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItemList, CartItemListener listener) {
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    // Hàm để Activity cập nhật danh sách mới cho adapter
    public void submitList(List<CartItem> newItems) {
        this.cartItemList = newItems;
        notifyDataSetChanged(); // Để đơn giản, dùng notifyDataSetChanged()
    }

    @NonNull @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cần đảm bảo layout item_cart có các View Button +/- và TextView cho số lượng
        // đã được thêm vào (trong XML của bạn chưa có)

        
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.productName.setText(item.getName());
        holder.productPrice.setText(String.format(Locale.US, "$%.2f", item.getPrice()));
        holder.productSize.setText(item.getSize());
        holder.productImage.setImageResource(item.getImageResId());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));

        holder.buttonPlus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            listener.onQuantityChanged(item, newQuantity);
        });

        holder.buttonMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                listener.onQuantityChanged(item, newQuantity);
            }
        });

        holder.deleteIcon.setOnClickListener(v -> {
            listener.onItemRemoved(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    // ViewHolder cần có đủ các View
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, deleteIcon;
        TextView productName, productPrice, productSize, quantityText;
        Button buttonPlus, buttonMinus; // Cần thêm các ID này vào file item_cart.xml

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productSize = itemView.findViewById(R.id.size_background);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            // Giả sử bạn đã thêm các view này vào item_cart.xml trong LinearLayout quantity_selector
            quantityText = itemView.findViewById(R.id.quantity_text);
            buttonPlus = itemView.findViewById(R.id.button_plus);
            buttonMinus = itemView.findViewById(R.id.button_minus);
        }
    }
}