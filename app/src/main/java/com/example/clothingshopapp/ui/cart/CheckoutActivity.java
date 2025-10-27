package com.example.clothingshopapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.data.model.OrderItem;
import com.example.clothingshopapp.data.remote.CartManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private static final int ADD_ADDRESS_REQUEST = 1;

    private TextView amountValue, shippingAddressText;
    private Button payButton;
    private RadioGroup paymentRadioGroup;
    private double totalAmount = 0;
    private final double shippingFee = 30000;
    private String currentAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        initViews();
        calculateAndDisplayTotals();
        loadUserAddress();
        setupListeners();
    }

    private void initViews() {
        amountValue = findViewById(R.id.amount_value);
        payButton = findViewById(R.id.pay_button);
        shippingAddressText = findViewById(R.id.shippingAddressText);
        paymentRadioGroup = findViewById(R.id.payment_radio_group);
    }

    private void loadUserAddress() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userAddressRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("address");

        userAddressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue(String.class) != null && !dataSnapshot.getValue(String.class).isEmpty()) {
                    currentAddress = dataSnapshot.getValue(String.class);
                    shippingAddressText.setText(currentAddress);
                    shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));
                } else {
                    currentAddress = null;
                    shippingAddressText.setText("Please add a shipping address...");
                    shippingAddressText.setTextColor(getResources().getColor(R.color.coral));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CheckoutActivity.this, "Failed to load address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayTotals() {
        double subtotal = CartManager.getInstance().getSubtotal();
        totalAmount = subtotal + shippingFee;// Giả sử không có phí ship
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        amountValue.setText(formatter.format(totalAmount));
    }

    private void setupListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        payButton.setOnClickListener(v -> placeOrder());
        findViewById(R.id.address_card).setOnClickListener(v -> showEditAddressDialog());
    }

    private void showEditAddressDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_address, null);
        final EditText addressInput = dialogView.findViewById(R.id.addressInput);

        if (currentAddress != null) {
            addressInput.setText(currentAddress);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Shipping Address")
                .setView(dialogView)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> {
                    String newAddress = addressInput.getText().toString().trim();
                    if (newAddress.isEmpty()) {
                        Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        saveAddressToFirebase(newAddress);
                    }
                })
                .show();
    }

    private void saveAddressToFirebase(String newAddress) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userAddressRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("address");

        userAddressRef.setValue(newAddress).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Address updated!", Toast.LENGTH_SHORT).show();
                currentAddress = newAddress;
                shippingAddressText.setText(currentAddress);
                shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));
            } else {
                Toast.makeText(this, "Failed to update address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        if (selectedRadioButton != null) {
            return selectedRadioButton.getText().toString();
        }
        return "Not selected";
    }

    private void placeOrder() {
        if (currentAddress == null || currentAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) { return; }

        List<CartItem> cartItems = CartManager.getInstance().getCartItemsLiveData().getValue();
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String orderId = ordersRef.push().getKey();
        String userId = currentUser.getUid();

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            orderItems.add(new OrderItem(
                    cartItem.getProduct().getProductId(), cartItem.getProduct().getName(),
                    cartItem.getVariant().getColor(), cartItem.getSize(), cartItem.getQuantity(),
                    cartItem.getProduct().getBasePrice(), cartItem.getVariant().getImageUrl()
            ));
        }

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTimestamp(System.currentTimeMillis());
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");
        order.setShippingAddress(currentAddress);
        order.setPaymentMethod(getSelectedPaymentMethod());
        order.setItems(orderItems);

        if (orderId != null) {
            ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    CartManager.getInstance().clearCart();
                    Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Đặt hàng thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}