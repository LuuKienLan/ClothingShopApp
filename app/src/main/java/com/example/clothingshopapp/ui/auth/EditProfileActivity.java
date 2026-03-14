package com.example.clothingshopapp.ui.auth;

import android.os.Bundle;
import android.util.Log; // ⭐ 1. THÊM IMPORT
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clothingshopapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore; // ⭐ 1. THÊM IMPORT

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private MaterialButton saveButton;
    private MaterialToolbar toolbar;
    private FirebaseUser currentUser;

    // ⭐ 2. THÊM BIẾN FIRESTORE
    private FirebaseFirestore db;
    private static final String TAG = "EditProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance(); // ⭐ 3. KHỞI TẠO FIRESTORE

        initViews();
        loadCurrentData();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nameInput = findViewById(R.id.nameInput);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadCurrentData() {
        if (currentUser != null) {
            nameInput.setText(currentUser.getDisplayName());
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> updateProfile());
    }

    // ⭐⭐⭐ BẮT ĐẦU SỬA (HÀM UPDATE) ⭐⭐⭐
    /**
     * Hàm này được viết lại để cập nhật 3 nơi:
     * 1. Firebase Auth (cho "Hi, trieu")
     * 2. Firestore Collection "Users" (cho hồ sơ)
     * 3. Firestore Collection "Chats" (cho Shop xem)
     */
    private void updateProfile() {
        String newName = nameInput.getText().toString().trim();

        if (newName.isEmpty()) {
            nameInput.setError("Tên không được để trống");
            nameInput.requestFocus();
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khóa nút lại để tránh nhấn spam
        saveButton.setEnabled(false);
        saveButton.setText("ĐANG LƯU...");

        String uid = currentUser.getUid();

        // --- BƯỚC 1: CẬP NHẬT FIREBASE AUTH ---
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdates).addOnCompleteListener(taskAuth -> {
            if (!taskAuth.isSuccessful()) {
                // Lỗi Auth (hiếm gặp)
                Toast.makeText(EditProfileActivity.this, "Lỗi khi cập nhật hồ sơ Auth.", Toast.LENGTH_SHORT).show();
                enableSaveButton(); // Mở nút
                return; // Dừng lại
            }

            Log.d(TAG, "Auth profile updated successfully.");

            // --- BƯỚC 2: CẬP NHẬT FIRESTORE "USERS" COLLECTION ---
            // (Chúng ta dùng .update() để cập nhật 1 trường, thay vì .set() (ghi đè))
            db.collection("Users").document(uid).update("fullName", newName)
                    .addOnCompleteListener(taskUser -> {
                        if (!taskUser.isSuccessful()) {
                            Log.w(TAG, "Auth đã cập nhật, nhưng lỗi khi cập nhật 'Users' collection.", taskUser.getException());
                            // (Không sao, vẫn tiếp tục bước 3)
                        } else {
                            Log.d(TAG, "'Users' collection updated successfully.");
                        }

                        // --- BƯỚC 3: CẬP NHẬT FIRESTORE "CHATS" COLLECTION ---
                        // (Đây là bước quan trọng nhất để Shop thấy tên mới)
                        // Tìm tất cả các phòng chat (Chats) mà user này tham gia
                        db.collection("Chats").whereArrayContains("participants", uid)
                                .get()
                                .addOnCompleteListener(taskChat -> {
                                    if (!taskChat.isSuccessful()) {
                                        // Lỗi nghiêm trọng, không tìm thấy chat
                                        Log.e(TAG, "Lỗi khi tìm phòng chat:", taskChat.getException());
                                        Toast.makeText(EditProfileActivity.this, "Hồ sơ đã cập nhật (Auth/User) nhưng lỗi khi cập nhật phòng chat.", Toast.LENGTH_LONG).show();
                                        enableSaveButton();
                                        finish(); // Vẫn thoát ra
                                        return;
                                    }

                                    if (taskChat.getResult() == null || taskChat.getResult().isEmpty()) {
                                        // User này chưa chat bao giờ
                                        Log.d(TAG, "User này chưa có phòng chat. Cập nhật hoàn tất.");
                                    } else {
                                        // Đã tìm thấy phòng chat -> Lặp qua và cập nhật
                                        for (com.google.firebase.firestore.DocumentSnapshot doc : taskChat.getResult()) {
                                            String chatId = doc.getId();
                                            db.collection("Chats").document(chatId).update("userFullName", newName);
                                        }
                                        Log.d(TAG, "Đã cập nhật " + taskChat.getResult().size() + " phòng chat.");
                                    }

                                    // HOÀN TẤT CẢ 3 BƯỚC
                                    Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                                    enableSaveButton();
                                    finish(); // Quay về
                                });
                    });
        });
    }

    /**
     * Hàm tiện ích để mở lại nút Save
     */
    private void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setText("SAVE");
    }

    // ⭐⭐⭐ KẾT THÚC SỬA (HÀM UPDATE) ⭐⭐⭐
}