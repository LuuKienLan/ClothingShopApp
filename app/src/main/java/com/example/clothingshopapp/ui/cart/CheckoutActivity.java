package com.example.clothingshopapp.ui.cart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Order;
import com.example.clothingshopapp.data.model.OrderItem;
// (Không cần import Product, Size, Variant)
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.ui.adapter.CartAdapter;
import com.example.clothingshopapp.ui.orders.OrderDetailActivity;
import com.example.clothingshopapp.ui.product.HomeActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// (Không cần import Transaction)
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";

    private TextView shippingAddressText;
    private TextView subtotalValue;
    private TextView shippingFeeValue;
    private TextView amountValue;
    private TextInputEditText phoneInput;

    private Button payButton;
    private RadioGroup paymentRadioGroup;
    private ImageButton myLocationButton;

    private RecyclerView checkoutItemsRecyclerView;
    private CartAdapter cartAdapter;

    private double totalAmount = 0;
    private final double shippingFee = 30000;

    private String currentAddress = null;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

    private List<CartItem> itemsToCheckout;
    private boolean isBuyNowFlow;

    private ActivityResultLauncher<Intent> mapAddressLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    // (Bỏ biến productsRef)
    // private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // (Bỏ khởi tạo productsRef)

        // (Code lấy Intent và GPS giữ nguyên)
        try {
            itemsToCheckout = (List<CartItem>) getIntent().getSerializableExtra("CHECKOUT_ITEMS");
            isBuyNowFlow = getIntent().getBooleanExtra("IS_BUY_NOW", false);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: Không có sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        if (itemsToCheckout == null || itemsToCheckout.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        fetchCurrentLocation();
                    } else {
                        Toast.makeText(this, "Không có quyền, không lấy vị trí được!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        initViews();
        setupRecyclerView();
        calculateAndDisplayTotals();
        loadUserAddress();
        loadUserPhone();
        setupListeners();

        mapAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("SELECTED_ADDRESS");
                        selectedLatitude = result.getData().getDoubleExtra("LATITUDE", 0.0);
                        selectedLongitude = result.getData().getDoubleExtra("LONGITUDE", 0.0);
                        if (address != null && !address.isEmpty()) {
                            currentAddress = address;
                            shippingAddressText.setText(currentAddress);
                            shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));
                            saveAddressToFirebase(currentAddress, selectedLatitude, selectedLongitude);
                        }
                    }
                }
        );
    }

    private void initViews() {
        payButton = findViewById(R.id.pay_button);
        shippingAddressText = findViewById(R.id.shippingAddressText);
        paymentRadioGroup = findViewById(R.id.payment_radio_group);
        myLocationButton = findViewById(R.id.myLocationButton);
        checkoutItemsRecyclerView = findViewById(R.id.checkoutItemsRecyclerView);
        subtotalValue = findViewById(R.id.subtotal_value);
        shippingFeeValue = findViewById(R.id.shipping_fee_value);
        amountValue = findViewById(R.id.amount_value);
        phoneInput = findViewById(R.id.phone_input_edit_text);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(null, true);
        checkoutItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutItemsRecyclerView.setAdapter(cartAdapter);
        cartAdapter.submitList(itemsToCheckout);
    }

    private void setupListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        payButton.setOnClickListener(v -> handlePayment());

        myLocationButton.setOnClickListener(v -> {
            checkAndRequestLocationPermission();
        });

        findViewById(R.id.edit_address_icon).setOnClickListener(v -> {
            showEditAddressDialog();
        });

        shippingAddressText.setOnClickListener(v -> {
            showEditAddressDialog();
        });
    }

    private void showEditAddressDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Nhập địa chỉ giao hàng");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_address, null);
        final TextInputEditText input = dialogView.findViewById(R.id.addressInput);
        input.setText(currentAddress);
        builder.setView(dialogView);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newAddressString = input.getText().toString().trim();
            if (newAddressString.isEmpty()) {
                Toast.makeText(this, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đang xác thực địa chỉ...", Toast.LENGTH_SHORT).show();
                new ForwardGeocodingTask().execute(newAddressString);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void loadUserAddress() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("address").exists() && dataSnapshot.child("address").getValue(String.class) != null) {
                    currentAddress = dataSnapshot.child("address").getValue(String.class);
                    selectedLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    selectedLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    shippingAddressText.setText(currentAddress);
                    shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));
                } else {
                    currentAddress = null; selectedLatitude = null; selectedLongitude = null;
                    shippingAddressText.setText("Vui lòng chọn địa chỉ giao hàng...");
                    shippingAddressText.setTextColor(getResources().getColor(R.color.coral));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CheckoutActivity.this, "Failed to load address.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPhone() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("phone");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue(String.class) != null) {
                    String phone = dataSnapshot.getValue(String.class);
                    phoneInput.setText(phone);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to load user phone.", databaseError.toException());
            }
        });
    }


    private void calculateAndDisplayTotals() {
        double subtotal = 0;
        for (CartItem item : itemsToCheckout) {
            subtotal += item.getProduct().getBasePrice() * item.getQuantity();
        }
        totalAmount = subtotal + shippingFee;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        subtotalValue.setText(formatter.format(subtotal));
        shippingFeeValue.setText(formatter.format(shippingFee));
        amountValue.setText(formatter.format(totalAmount));
    }

    private void openMapAddressSelection() {
        Intent intent = new Intent(this, MapAddressSelectionActivity.class);
        mapAddressLauncher.launch(intent);
    }

    private void saveAddressToFirebase(String newAddress, double lat, double lng) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
        userRef.child("address").setValue(newAddress);
        userRef.child("latitude").setValue(lat);
        userRef.child("longitude").setValue(lng);
        Log.d(TAG, "Address updated successfully in Firebase.");
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentRadioGroup.getCheckedRadioButtonId();
        if(selectedId == R.id.radio_cod) {
            return "Cash on Delivery (COD)";
        } else if(selectedId == R.id.radio_vnpay) {
            return "VNPAY";
        }
        return "Not selected";
    }

    private void handlePayment() {
        if (currentAddress == null || currentAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String phoneNumber = phoneInput.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            phoneInput.setError("Vui lòng nhập số điện thoại");
            phoneInput.requestFocus();
            return;
        }
        if (!isValidVietnamesePhone(phoneNumber)) {
            phoneInput.setError("Số điện thoại không hợp lệ (phải 10 số, bắt đầu 03,05,07,08,09)");
            phoneInput.requestFocus();
            return;
        }
        String paymentMethod = getSelectedPaymentMethod();
        String orderId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 15);
        createPendingOrder(orderId, paymentMethod, phoneNumber, () -> {
            if (paymentMethod.equalsIgnoreCase("VNPAY")) {
                callLocalVnpayHelper(orderId, totalAmount);
            }
        });
    }

    interface OrderCreationCallback {
        void onOrderCreated();
    }

    // ⭐ SỬA: BỎ LOGIC "TRỪ KHO"
    private void createPendingOrder(String orderId, String paymentMethod, String phoneNumber, OrderCreationCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || itemsToCheckout == null || itemsToCheckout.isEmpty()) {
            Toast.makeText(this, "Lỗi: Người dùng hoặc sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String userId = currentUser.getUid();
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : itemsToCheckout) {
            orderItems.add(new OrderItem(
                    cartItem.getProduct().getProductId(), cartItem.getProduct().getName(),
                    cartItem.getVariant().getColor(), cartItem.getSize(), cartItem.getQuantity(),
                    cartItem.getProduct().getBasePrice(), cartItem.getVariant().getFirstImageUrl()
            ));
        }
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTimestamp(System.currentTimeMillis());
        order.setTotalAmount(totalAmount);
        order.setStatus(paymentMethod.equals("VNPAY") ? "PendingPayment" : "Pending");
        order.setShippingAddress(currentAddress);
        order.setPaymentMethod(paymentMethod);
        order.setPhoneNumber(phoneNumber);
        order.setItems(orderItems);
        order.setIsBuyNow(isBuyNowFlow);
        if (selectedLatitude != null) order.setLatitude(selectedLatitude);
        if (selectedLongitude != null) order.setLongitude(selectedLongitude);
        order.setShippingFee(shippingFee);

        ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // (BỎ GỌI HÀM TRỪ KHO)
                // decreaseStockFromCart();

                savePhoneToUserProfile(phoneNumber);
                if (!isBuyNowFlow) {
                    CartManager.getInstance().clearCart();
                }
                if (paymentMethod.equalsIgnoreCase("Cash on Delivery (COD)")) {
                    Intent intent = new Intent(CheckoutActivity.this, OrderConfirmationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    callback.onOrderCreated();
                }
            } else {
                Toast.makeText(this, "Đặt hàng thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ⭐⭐⭐ XÓA 3 HÀM "TRỪ KHO" (BẢN LỖI) ⭐⭐⭐
    // (Xóa: decreaseStockFromCart, findStockRefPath, runStockTransaction)


    private void savePhoneToUserProfile(String phoneNumber) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        DatabaseReference userPhoneRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("phone");
        userPhoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    userPhoneRef.setValue(phoneNumber);
                    Log.d(TAG, "Lưu SĐT mới vào hồ sơ user.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Lỗi khi check SĐT user", databaseError.toException());
            }
        });
    }

    private boolean isValidVietnamesePhone(String phone) {
        String vietnamPhoneRegex = "^(0[3|5|7|8|9])([0-9]{8})$";
        return Pattern.matches(vietnamPhoneRegex, phone);
    }

    private void callLocalVnpayHelper(String orderId, double amount) {
        long amountToPay = (long) amount;
        String paymentUrl = VNPAYHelper.createPaymentUrl(this, orderId, amountToPay);
        if (paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi khi tạo link thanh toán.", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đang chuyển đến VNPAY...", Toast.LENGTH_SHORT).show();
        vnpayLaunched = true;
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(paymentUrl));
    }
    private boolean vnpayLaunched = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (vnpayLaunched) {
            vnpayLaunched = false;
            Toast.makeText(this, "Thanh toán đã bị hủy.", Toast.LENGTH_SHORT).show();
            goToHome();
        }
    }
    private void goToHome() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeIntent);
        finish();
    }

    // Logic "bắn" GPS (giữ nguyên)
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Quyền đã có. Đang lấy vị trí...");
            fetchCurrentLocation();
        } else {
            Log.d(TAG, "Chưa có quyền. Đang xin quyền...");
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }


    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        myLocationButton.setEnabled(false);
        Toast.makeText(this, "Đang lấy vị trí hiện tại...", Toast.LENGTH_SHORT).show();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    myLocationButton.setEnabled(true);
                    if (location != null) {
                        Log.d(TAG, "Lấy vị trí thành công: " + location.getLatitude() + ", " + location.getLongitude());
                        new ReverseGeocodingTask().execute(location);
                    } else {
                        Log.w(TAG, "Không thể lấy vị trí (null).");
                        Toast.makeText(this, "Không tìm thấy vị trí. Bạn vui lòng bật GPS!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    myLocationButton.setEnabled(true);
                    Log.e(TAG, "Lỗi khi lấy vị trí: ", e);
                    Toast.makeText(this, "Lỗi dịch vụ GPS.", Toast.LENGTH_SHORT).show();
                });
    }

    // Hàm "Dịch ngược" (GPS -> Chữ)
    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Address> {
        private double lat, lng;

        @Override
        protected Address doInBackground(Location... params) {
            Location location = params[0];
            lat = location.getLatitude();
            lng = location.getLongitude();
            Geocoder geocoder = new Geocoder(CheckoutActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses.get(0);
                } else { return null; }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi 'máy dịch' GPS", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address == null) {
                Toast.makeText(CheckoutActivity.this, "Lỗi: Không thể dịch tọa độ ra địa chỉ.", Toast.LENGTH_SHORT).show();
                return;
            }
            String countryCode = address.getCountryCode();
            Log.d(TAG, "Địa chỉ dịch được: " + address.getAddressLine(0));
            Log.d(TAG, "Quốc gia của địa chỉ: " + countryCode);

            if (countryCode != null && countryCode.equalsIgnoreCase("VN")) {
                String resultAddress = address.getAddressLine(0);
                currentAddress = resultAddress;
                selectedLatitude = lat;
                selectedLongitude = lng;
                shippingAddressText.setText(currentAddress);
                shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));
                saveAddressToFirebase(currentAddress, selectedLatitude, selectedLongitude);
                Toast.makeText(CheckoutActivity.this, "Đã cập nhật địa chỉ!", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Địa chỉ bị từ chối vì ở ngoài Việt Nam (CountryCode: " + countryCode + ")");
                Toast.makeText(CheckoutActivity.this, "Lỗi: Chỉ hỗ trợ giao hàng trong Việt Nam.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Hàm "Dịch xuôi" (Chữ -> GPS)
    private class ForwardGeocodingTask extends AsyncTask<String, Void, Address> {
        private String typedAddress;

        @Override
        protected Address doInBackground(String... params) {
            typedAddress = params[0];
            Geocoder geocoder = new Geocoder(CheckoutActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(typedAddress + ", Vietnam", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses.get(0);
                } else {
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi 'máy dịch' (dịch xuôi)", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address == null) {
                Toast.makeText(CheckoutActivity.this, "Không tìm thấy địa chỉ này. Vui lòng gõ chính xác hơn.", Toast.LENGTH_LONG).show();
                return;
            }

            String countryCode = address.getCountryCode();
            Log.d(TAG, "Địa chỉ gõ tay dịch được: " + address.getAddressLine(0));
            Log.d(TAG, "Quốc gia của địa chỉ: " + countryCode);

            if (countryCode != null && countryCode.equalsIgnoreCase("VN")) {
                String resultAddress = address.getAddressLine(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();

                currentAddress = resultAddress;
                selectedLatitude = lat;
                selectedLongitude = lng;

                shippingAddressText.setText(currentAddress);
                shippingAddressText.setTextColor(getResources().getColor(R.color.dark_navy));

                saveAddressToFirebase(currentAddress, selectedLatitude, selectedLongitude);
                Toast.makeText(CheckoutActivity.this, "Đã cập nhật địa chỉ!", Toast.LENGTH_SHORT).show();

            } else {
                Log.w(TAG, "Địa chỉ gõ tay bị từ chối (CountryCode: " + countryCode + ")");
                Toast.makeText(CheckoutActivity.this, "Lỗi: Địa chỉ gõ tay phải ở trong Việt Nam.", Toast.LENGTH_LONG).show();
            }
        }
    }
}