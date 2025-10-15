package com.example.clothingshopapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.clothingshopapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private CheckBox rememberMeCheckbox;
    private MaterialButton loginButton;
    private TextView signUpText, forgotPasswordText;
    private CardView facebookLogin, twitterLogin, appleLogin;

    // Tài khoản test
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        facebookLogin = findViewById(R.id.facebookLogin);
        twitterLogin = findViewById(R.id.twitterLogin);
        appleLogin = findViewById(R.id.appleLogin);
    }

    private void setupListeners() {
        // Login button
        loginButton.setOnClickListener(v -> handleLogin());

        // Sign up text
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password
        forgotPasswordText.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng quên mật khẩu", Toast.LENGTH_SHORT).show();
        });

        // Social logins
        facebookLogin.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập bằng Facebook", Toast.LENGTH_SHORT).show()
        );

        twitterLogin.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập bằng Twitter", Toast.LENGTH_SHORT).show()
        );

        appleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập bằng Apple", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tài khoản test
        if (email.equals(TEST_EMAIL) && password.equals(TEST_PASSWORD)) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển sang màn hình Home
            Intent intent = new Intent(LoginActivity.this, com.example.clothingshopapp.ui.product.ProductViewModel.class);
            intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this,
                    "Email hoặc mật khẩu không đúng!\nThử: test@gmail.com / 123456",
                    Toast.LENGTH_LONG).show();
        }
    }
}