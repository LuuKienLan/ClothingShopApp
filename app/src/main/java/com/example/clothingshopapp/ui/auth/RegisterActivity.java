package com.example.clothingshopapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.clothingshopapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Import để bắt lỗi Email trùng
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, retypePasswordInput;
    private MaterialButton signUpButton;
    private CardView backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        retypePasswordInput = findViewById(R.id.retypePasswordInput);
        signUpButton = findViewById(R.id.signUpButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        signUpButton.setOnClickListener(v -> validateAndHandleSignUp());
    }


    /**
     * Hàm chính để xử lý đăng ký, bao gồm tất cả các bước kiểm tra.
     */
    private void validateAndHandleSignUp() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String retypePassword = retypePasswordInput.getText().toString().trim();

        // --- BƯỚC 1: KIỂM TRA DỮ LIỆU ĐẦU VÀO (Đồng bộ) ---
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Regex chỉ cho phép chữ cái, số, và dấu gạch dưới (không khoảng trắng)
        String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
        if (!name.matches(USERNAME_PATTERN)) {
            nameInput.setError("Tên không hợp lệ! Chỉ dùng chữ, số, và dấu gạch dưới (_).");
            nameInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email không hợp lệ");
            emailInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Mật khẩu phải có ít nhất 6 ký tự");
            passwordInput.requestFocus();
            return;
        }
        if (!password.equals(retypePassword)) {
            retypePasswordInput.setError("Mật khẩu không khớp");
            retypePasswordInput.requestFocus();
            return;
        }

        // --- BƯỚC 2: KHÓA NÚT & BẮT ĐẦU KIỂM TRA MẠNG (Bất đồng bộ) ---
        signUpButton.setEnabled(false);
        signUpButton.setText("ĐANG KIỂM TRA..."); // Phản hồi UI

        // --- BƯỚC 3: KIỂM TRA TÊN (USERNAME) TRÊN FIRESTORE ---
        // Chúng ta phải làm điều này thủ công vì Auth không check tên
        db.collection("Users").whereEqualTo("fullName", name).get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // LỖI: Tên đã tồn tại
                            Log.w(TAG, "Username '" + name + "' đã tồn tại.");
                            Toast.makeText(RegisterActivity.this, "Tên này đã được sử dụng. Vui lòng chọn tên khác.", Toast.LENGTH_LONG).show();
                            nameInput.setError("Tên đã tồn tại");
                            nameInput.requestFocus();
                            enableSignUpButton(); // Mở nút

                        } else {
                            // THÀNH CÔNG: Tên là duy nhất. Tiếp tục kiểm tra Email.
                            Log.d(TAG, "Username '" + name + "' là duy nhất, tiếp tục check email...");

                            // --- BƯỚC 4: KIỂM TRA EMAIL & TẠO TÀI KHOẢN AUTH ---
                            createUserInAuth(name, email, password);
                        }

                    } else {
                        // LỖI: Không thể kết nối Firestore để check tên
                        Log.e(TAG, "Lỗi khi check username: ", task.getException());
                        Toast.makeText(RegisterActivity.this, "Lỗi khi kiểm tra tên: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        enableSignUpButton(); // Mở nút
                    }
                });
    }

    /**
     * Hàm này chỉ được gọi sau khi đã check tên (username) thành công.
     * Nó sẽ tạo tài khoản trên Firebase Auth và bắt lỗi nếu Email trùng.
     */
    private void createUserInAuth(String name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        // --- BƯỚC 5: TẠO AUTH THÀNH CÔNG ---
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();

                        // Cập nhật "Căn cước" (Auth Profile) với tên mới
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        firebaseUser.updateProfile(profileUpdates); // Chạy ngầm, không cần chờ

                        // --- BƯỚC 6: TẠO "HỒ SƠ" (FIRESTORE DOCUMENT) ---
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", name); // Lưu tên
                        user.put("email", email);
                        user.put("avatarUrl", "");
                        user.put("role", "user");

                        db.collection("Users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // HOÀN TẤT
                                    Log.d(TAG, "User Profile created in Firestore");
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Lỗi khi tạo hồ sơ (hiếm gặp)
                                    Log.w(TAG, "Error creating user profile", e);
                                    Toast.makeText(RegisterActivity.this, "Lỗi khi tạo hồ sơ: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    enableSignUpButton(); // Mở nút
                                });

                    } else {
                        // --- BƯỚC 5 THẤT BẠI (LỖI KHI TẠO AUTH) ---
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                        try {
                            // Bắt chính xác lỗi để thông báo
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            // LỖI: Email đã tồn tại
                            Log.w(TAG, "Email '" + email + "' đã tồn tại.");
                            Toast.makeText(RegisterActivity.this, "Email này đã được đăng ký. Vui lòng sử dụng email khác.", Toast.LENGTH_LONG).show();
                            emailInput.setError("Email đã tồn tại");
                            emailInput.requestFocus();
                        } catch (Exception e) {
                            // Lỗi khác (mạng yếu, mật khẩu yếu...)
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                        enableSignUpButton(); // Mở nút
                    }
                });
    }

    /**
     * Hàm tiện ích để mở lại nút Sign Up
     */
    private void enableSignUpButton() {
        if (signUpButton != null) {
            signUpButton.setEnabled(true);
            signUpButton.setText("SIGN UP");
        }
    }
}