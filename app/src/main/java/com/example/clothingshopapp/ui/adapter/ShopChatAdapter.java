package com.example.clothingshopapp.ui.adapter; // (Package của "bố")

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface; // <-- "VÁ" (IMPORT)
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.Conversation;
import com.example.clothingshopapp.ui.chat.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
// (Import thư viện Glide hoặc Picasso nếu "bố" dùng để load ảnh)
// import com.bumptech.glide.Glide;

public class ShopChatAdapter extends FirestoreRecyclerAdapter<Conversation, ShopChatAdapter.ChatViewHolder> {

    public ShopChatAdapter(@NonNull FirestoreRecyclerOptions<Conversation> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Conversation model) {

        // ***** "PHẪU THUẬT" (FIX "BỆNH" 3 - TÊN "ád") *****
        // "BỐ" NHÌN KỸ 2 DÒNG NÀY:

        // 1. "Vẽ" Tên (DÒNG 1)
        String customerName = model.getUserFullName(); // Lấy Tên ("Jack" hoặc null)
        if (customerName == null || customerName.trim().isEmpty()) {
            holder.nameTextView.setText("Khách hàng"); // Nếu tên "ma", hiện "Khách hàng"
        } else {
            holder.nameTextView.setText(customerName); // Nếu có tên, hiện "Jack"
        }

        // 2. "Vẽ" Tin nhắn cuối (DÒNG 2)
        holder.lastMessageTextView.setText(model.getLastMessage());


        // (Code "vẽ" cái bong bóng số (5) y hệt cũ)
        if (model.getShopUnreadCount() > 0) {
            holder.unreadCountTextView.setText(String.valueOf(model.getShopUnreadCount()));
            holder.unreadCountTextView.setVisibility(View.VISIBLE);
            holder.lastMessageTextView.setTypeface(null, Typeface.BOLD);
            holder.nameTextView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.unreadCountTextView.setVisibility(View.GONE);
            holder.lastMessageTextView.setTypeface(null, Typeface.NORMAL);
            holder.nameTextView.setTypeface(null, Typeface.NORMAL);
        }

        // (Code load ảnh...)

        // (Code "dạy" Click - "Bệnh" 4 - y hệt cũ)
        holder.itemView.setOnClickListener(v -> {
            String chatId = getSnapshots().getSnapshot(position).getId();
            String title = holder.nameTextView.getText().toString();

            Log.d("ShopChatAdapter", "Click vào phòng: " + chatId);

            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ChatActivity.class);

            intent.putExtra("CHAT_ID", chatId);
            intent.putExtra("CHAT_TITLE", title);

            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_conversation, parent, false);
        return new ChatViewHolder(view);
    }

    // Class ViewHolder (Giữ nguyên)
    class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView; // <-- Thằng này là Dòng 1
        TextView lastMessageTextView; // <-- Thằng này là Dòng 2
        TextView unreadCountTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView); //
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            unreadCountTextView = itemView.findViewById(R.id.unreadCountTextView);
        }
    }
}