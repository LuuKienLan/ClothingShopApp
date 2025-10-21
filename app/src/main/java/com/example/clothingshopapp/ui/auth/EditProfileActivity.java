package com.example.clothingshopapp.ui.auth;

import android.os.Bundle;
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

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText nameInput;
    private MaterialButton saveButton;
    private MaterialToolbar toolbar;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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

    private void updateProfile() {
        String newName = nameInput.getText().toString().trim();

        if (newName.isEmpty()) {
            nameInput.setError("Name cannot be empty");
            return;
        }

        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Quay về màn hình Profile
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}