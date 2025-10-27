package com.example.clothingshopapp.ui.cart;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clothingshopapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddAddressActivity extends AppCompatActivity {

    private TextInputEditText addressInput;
    private Button saveAddressButton;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        initViews();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        addressInput = findViewById(R.id.addressInput);
        saveAddressButton = findViewById(R.id.saveAddressButton);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        saveAddressButton.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String address = addressInput.getText().toString().trim();
        if (address.isEmpty()) {
            addressInput.setError("Address cannot be empty");
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userAddressRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("address");

        userAddressRef.setValue(address).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Address saved!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Báo cho màn hình trước biết là đã lưu thành công
                finish(); // Đóng màn hình này
            } else {
                Toast.makeText(this, "Failed to save address.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}