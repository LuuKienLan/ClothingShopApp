package com.example.clothingshopapp.ui.auth;

// ⭐ 1. THÊM 2 DÒNG IMPORT NÀY
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.adapter.ShopChatAdapter;
import com.example.clothingshopapp.data.model.Conversation;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ShopAdminActivity extends AppCompatActivity {

    private static final String TAG = "ADMIN_DEBUG";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView chatRecyclerView;
    private ShopChatAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_admin);

        Log.d(TAG, "onCreate: Bắt đầu tạo ShopAdminActivity");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Chuẩn bị query...");
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "LỖI: User bị null, không thể query!");
            return;
        }
        String currentShopId = mAuth.getCurrentUser().getUid();
        Query query = db.collection("Chats")
                .whereArrayContains("participants", currentShopId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Conversation> options = new FirestoreRecyclerOptions.Builder<Conversation>()
                .setQuery(query, Conversation.class)
                .build();

        Log.d(TAG, "setupRecyclerView: Tạo mới ShopChatAdapter");
        adapter = new ShopChatAdapter(options);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        // (Code cũ của bạn, giữ nguyên)
        chatRecyclerView.setItemAnimator(null);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop_options_menu, menu);
        return true;
    }


    // ⭐⭐⭐ BẮT ĐẦU SỬA (THÊM POP-UP) ⭐⭐⭐

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // KHÔNG đăng xuất vội
            // Thay vào đó, gọi hàm hiển thị Pop-up
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * HÀM MỚI: Hiển thị Pop-up xác nhận
     */
    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất") // Tiêu đề
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?") // Thông điệp
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Nhấn "Hủy", không làm gì cả
                    dialog.dismiss();
                })
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Nhấn "Đăng xuất"

                    // 1. Đăng xuất khỏi Firebase
                    mAuth.signOut();

                    // 2. Hiển thị Toast (như bạn yêu cầu)
                    Toast.makeText(ShopAdminActivity.this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

                    // 3. Quay về màn hình Login
                    Intent intent = new Intent(ShopAdminActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show(); // Hiển thị pop-up
    }

    // ⭐⭐⭐ KẾT THÚC SỬA ⭐⭐⭐


    // (Các hàm onStart, onStop, onDestroy giữ nguyên)
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: Màn hình Admin BẮT ĐẦU. Chuẩn bị nghe...");

        if (adapter != null) {
            chatRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "onStart (post): GỌI adapter.startListening()");
                        adapter.startListening();
                    } catch (Exception e) {
                        Log.e(TAG, "!!!!!!!!!! BẮT ĐƯỢC LỖI KHI START LISTENING !!!!!!!!!!!");
                        Log.e(TAG, "LỖI LÀ: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "onStop: Màn hình Admin BỊ CHE KHUẤT. Ngừng nghe...");
        if (adapter != null) {
            try {
                Log.d(TAG, "onStop: GỌI adapter.stopListening()");
                adapter.stopListening();
            } catch (Exception e) {
                Log.e(TAG, "!!!!!!!!!! BẮT ĐƯỢC LỖI KHI STOP LISTENING !!!!!!!!!!!");
                Log.e(TAG, "LỖI LÀ: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (chatRecyclerView != null) {
            chatRecyclerView.removeCallbacks(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: Màn hình Admin BỊ HỦY");
    }
}