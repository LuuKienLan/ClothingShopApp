package com.example.clothingshopapp.ui.auth;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clothingshopapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText oldPasswordInput, newPasswordInput, confirmPasswordInput;
    private MaterialButton changePasswordButton;
    private MaterialToolbar toolbar;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        oldPasswordInput = findViewById(R.id.oldPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        changePasswordButton = findViewById(R.id.changePasswordButton);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        changePasswordButton.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String oldPassword = oldPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // --- Validate Inputs ---
        if (oldPassword.isEmpty()) {
            oldPasswordInput.setError("Old password is required");
            oldPasswordInput.requestFocus();
            return;
        }
        if (newPassword.length() < 6) {
            newPasswordInput.setError("Password must be at least 6 characters");
            newPasswordInput.requestFocus();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }
        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Step 1: Re-authenticate User ---
        // Create a credential with the user's email and the old password they provided.
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        // --- Step 2: Update Password (Only if re-authentication is successful) ---
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        // Handle errors, e.g., weak password
                                        Toast.makeText(ChangePasswordActivity.this, "Error: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Re-authentication failed (likely wrong old password)
                        Toast.makeText(ChangePasswordActivity.this, "Authentication failed. Please check your old password.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}