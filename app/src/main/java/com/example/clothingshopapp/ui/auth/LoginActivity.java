package com.example.clothingshopapp.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.ui.product.HomeActivity;
import com.example.clothingshopapp.ui.auth.ShopAdminActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView signUpText;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "LoginActivity";

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER = "remember";

    // ⭐⭐⭐ BẮT ĐẦU SỬA "LỖI NHÁY" ⭐⭐⭐
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. DỜI 2 DÒNG NÀY LÊN ĐẦU
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. "CHECK" NGAY LẬP TỨC (LẤY TỪ onStart DÁN VÀO)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // "Bố" ĐÃ ĐĂNG NHẬP
            Log.d(TAG, "User đã đăng nhập. Bỏ qua Login, check role ngay.");

            // *KHÔNG* GỌI setContentView()
            // (Đây là mấu chốt, nó sẽ không "vẽ" màn hình Login)

            checkUserRole(currentUser.getUid());
            // (Hàm checkUserRole sẽ tự gọi finish() và "giết" LoginActivity)

        } else {
            // "Bố" CHƯA ĐĂNG NHẬP
            Log.d(TAG, "User chưa đăng nhập. Hiển thị màn hình Login.");

            // 3. "VẼ" MÀN HÌNH LOGIN (NHƯ CŨ)
            setContentView(R.layout.activity_login);
            initViews();
            loadPreferences();
            setupListeners();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }



    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
    }

    private void loadPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean(PREF_REMEMBER, false);
        if (rememberMe) {
            emailInput.setText(prefs.getString(PREF_EMAIL, ""));
            passwordInput.setText(prefs.getString(PREF_PASSWORD, ""));
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (rememberMeCheckbox.isChecked()) {
                            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putString(PREF_EMAIL, email);
                            editor.putString(PREF_PASSWORD, password);
                            editor.putBoolean(PREF_REMEMBER, true);
                            editor.apply();
                        } else {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                        }

                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        checkUserRole(task.getResult().getUser().getUid());

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void goToHomeActivity() {
        CartManager.getInstance().initializeForUser();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // <--- Dòng này "giết" LoginActivity
    }


    private void checkUserRole(String uid) {
        DocumentReference userRef = db.collection("Users").document(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String role = document.getString("role");
                    Log.d(TAG, "User role is: [" + role + "]");
                    if (role != null && "shop".equals(role.trim())) {
                        goToShopAdminActivity();
                    } else {
                        goToHomeActivity();
                    }
                } else {
                    Log.w(TAG, "User document not found for UID: " + uid + ", defaulting to user.");
                    goToHomeActivity();
                }
            } else {
                Log.e(TAG, "Failed to get user role", task.getException());
                Toast.makeText(LoginActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                goToHomeActivity();
            }
        });
    }


    private void goToShopAdminActivity() {
        Intent intent = new Intent(LoginActivity.this, ShopAdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // <--- Dòng này "giết" LoginActivity
    }
}