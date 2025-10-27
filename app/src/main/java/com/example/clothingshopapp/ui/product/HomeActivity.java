package com.example.clothingshopapp.ui.product;

import com.example.clothingshopapp.ui.map.MapActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem; // ⭐ Import
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu; // ⭐ Import
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Product;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.data.repository.ProductRepository;
import com.example.clothingshopapp.ui.adapter.ProductAdapter;
import com.example.clothingshopapp.ui.auth.ProfileActivity;
import com.example.clothingshopapp.ui.cart.CartActivity;
import com.example.clothingshopapp.ui.orders.MyOrdersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private TextView cartBadge, greetingText;
    private ImageView userAvatar, filterIcon; // ⭐ filterIcon
    private FrameLayout cartContainer;
    private BottomNavigationView bottomNavigation;
    private EditText searchInput;
    private ProductRepository productRepository;
    private FirebaseAuth mAuth;

    private List<Product> originalProductList = new ArrayList<>();
    private int currentSortOptionId = R.id.sort_default; // ⭐ Lưu lựa chọn sort

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        productRepository = new ProductRepository();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            CartManager.getInstance().initializeForUser();
        }

        initViews();
        setupUserGreeting();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupFilterListener(); // ⭐ Listener cho icon filter để mở Sort Menu
        setupBottomNavigation();
        loadProductsFromFirebase();
        observeCartChanges();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        setupUserGreeting();
        // Cart badge updated via LiveData observer
    }

    private void initViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        cartBadge = findViewById(R.id.cartBadge);
        cartContainer = findViewById(R.id.cartContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        greetingText = findViewById(R.id.greetingText);
        userAvatar = findViewById(R.id.userAvatar);
        searchInput = findViewById(R.id.searchInput);
        filterIcon = findViewById(R.id.filterIcon); // ⭐ Ánh xạ icon filter
    }

    private void observeCartChanges() {
        CartManager cartManager = CartManager.getInstance();
        if (mAuth.getCurrentUser() != null) {
            LiveData<List<CartItem>> cartLiveData = cartManager.getCartItemsLiveData();
            if (cartLiveData != null) {
                cartLiveData.observe(this, this::updateCartBadge);
            } else {
                cartManager.initializeForUser(); // Try initializing again
                cartManager.getCartItemsLiveData().observe(this, this::updateCartBadge);
            }
        } else {
            updateCartBadge(new ArrayList<>()); // Show empty badge if not logged in
        }
    }

    private void setupUserGreeting() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.trim().isEmpty()) {
                name = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "User";
            }
            greetingText.setText("Hi, " + name + " 👋");
        } else {
            greetingText.setText("Welcome! Please sign in 👋");
        }
    }

    private void setupClickListeners() {
        cartContainer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });
        userAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSortProducts(); // Lọc khi gõ
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ⭐ Listener cho icon Filter để mở Sort Menu ⭐
    private void setupFilterListener() {
        filterIcon.setOnClickListener(v -> {
            showSortMenu(v); // Gọi hàm hiển thị PopupMenu
        });
    }

    // ⭐ HÀM MỚI: HIỂN THỊ POPUP MENU SẮP XẾP ⭐
    private void showSortMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        // Nạp menu từ file XML
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

        // Đặt listener khi một mục menu được chọn
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            // Chỉ cập nhật nếu lựa chọn thay đổi
            if (itemId != currentSortOptionId) {
                currentSortOptionId = itemId; // Lưu lại lựa chọn mới
                filterAndSortProducts(); // Áp dụng sắp xếp mới
                Toast.makeText(HomeActivity.this, "Sắp xếp theo: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        popup.show(); // Hiển thị menu
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, new ArrayList<>());
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void loadProductsFromFirebase() {
        productsRecyclerView.setVisibility(View.GONE);
        productRepository.getAllProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                originalProductList.clear();
                originalProductList.addAll(products);
                filterAndSortProducts(); // Áp dụng lọc/sắp xếp ban đầu
                productsRecyclerView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm: " + message, Toast.LENGTH_SHORT).show();
                productsRecyclerView.setVisibility(View.VISIBLE);
                filterAndSortProducts(); // Hiển thị list rỗng nếu lỗi
            }
        });
    }

    // ⭐ HÀM KẾT HỢP LỌC VÀ SẮP XẾP ⭐
    private void filterAndSortProducts() {
        String query = searchInput.getText().toString().toLowerCase().trim();
        List<Product> filteredList;

        // 1. Lọc theo Search Query
        if (query.isEmpty()) {
            filteredList = new ArrayList<>(originalProductList);
        } else {
            // Sử dụng Java 8 streams cho gọn
            filteredList = originalProductList.stream()
                    .filter(product -> product.getName() != null && product.getName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        // 2. Sắp xếp danh sách đã lọc dựa vào currentSortOptionId
        if (currentSortOptionId == R.id.sort_price_asc) {
            Collections.sort(filteredList, Comparator.comparingDouble(Product::getBasePrice));
        } else if (currentSortOptionId == R.id.sort_price_desc) {
            Collections.sort(filteredList, Comparator.comparingDouble(Product::getBasePrice).reversed());
        } else if (currentSortOptionId == R.id.sort_name_asc) {
            Collections.sort(filteredList, Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
        } else if (currentSortOptionId == R.id.sort_name_desc) {
            Collections.sort(filteredList, Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        }
        // else if (currentSortOptionId == R.id.sort_default) {
        // Không cần làm gì, giữ nguyên thứ tự sau khi lọc
        // }

        // 3. Cập nhật Adapter
        productAdapter.updateProducts(filteredList);
    }

    private void updateCartBadge(List<CartItem> cartItems) {
        if (cartItems == null) return;
        int itemCount = 0;
        for (CartItem item : cartItems) {
            itemCount += item.getQuantity();
        }
        if (itemCount > 0) {
            cartBadge.setText(String.valueOf(itemCount));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; // Already here
            }
            else if (itemId == R.id.nav_map) { // ⭐ THÊM ELSE IF NÀY ⭐
                    Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                    startActivity(intent);
                    return true; // Return true để đánh dấu item đã được xử lý

            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent intent = new Intent(HomeActivity.this, MyOrdersActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}