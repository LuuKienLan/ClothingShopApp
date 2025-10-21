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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView signUpText;
    private CheckBox rememberMeCheckbox;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER = "remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        loadPreferences();
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            goToHomeActivity();
        }
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (rememberMeCheckbox.isChecked()) {
                                // Save credentials
                                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString(PREF_EMAIL, email);
                                editor.putString(PREF_PASSWORD, password);
                                editor.putBoolean(PREF_REMEMBER, true);
                                editor.apply();
                            } else {
                                // Clear saved credentials
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                            }

                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            goToHomeActivity();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void goToHomeActivity() {
        CartManager.getInstance().initializeForUser();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}