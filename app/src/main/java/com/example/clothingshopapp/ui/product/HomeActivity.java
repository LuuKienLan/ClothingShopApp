package com.example.clothingshopapp.ui.product;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.example.clothingshopapp.ui.chat.ChatActivity;
import com.example.clothingshopapp.ui.map.MapActivity;
import com.example.clothingshopapp.ui.orders.MyOrdersActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity
        implements FilterBottomSheetDialogFragment.FilterListener, SortBottomSheetDialogFragment.SortListener {

    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private TextView cartBadge, greetingText;
    private ImageView userAvatar, filterIcon;
    private TextView textSortBy;
    private FrameLayout cartContainer;
    private BottomNavigationView bottomNavigation;
    private EditText searchInput;
    private ProductRepository productRepository;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private ListenerRegistration chatListenerRegistration;
    private static final String SHOP_UID = "leTUI0RSJGaAZDLe0qZhC5LlP1p2";

    private View loadingOverlay;
    private List<Product> originalProductList = new ArrayList<>();
    private int currentSortOptionId = R.id.sort_default;
    private String currentCategoryFilter = "Tất cả";
    private int currentPriceRangeId = R.id.chipPriceAll;
    private FilterSortTask currentFilterTask;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        productRepository = new ProductRepository();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupPermissionLauncher();
        askForNotificationPermission();
        setupUserGreeting();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupSortListener();
        setupFilterListener();
        setupBottomNavigation();
        loadProductsFromFirebase();
        observeCartChanges();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(this, "Đã cấp quyền thông báo!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn sẽ không nhận được thông báo giỏ hàng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        setupUserGreeting();
        setupChatBadgeListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentFilterTask != null && currentFilterTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentFilterTask.cancel(true);
            currentFilterTask = null;
        }
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
            chatListenerRegistration = null;
        }
    }

    private void initViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        cartBadge = findViewById(R.id.cartBadge);
        cartContainer = findViewById(R.id.cartContainer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        greetingText = findViewById(R.id.greetingText);
        userAvatar = findViewById(R.id.userAvatar);
        searchInput = findViewById(R.id.searchInput);
        filterIcon = findViewById(R.id.filterIcon);
        textSortBy = findViewById(R.id.textSortBy);
    }

    private void observeCartChanges() {
        CartManager cartManager = CartManager.getInstance();
        if (mAuth.getCurrentUser() != null) {
            LiveData<List<CartItem>> cartLiveData = cartManager.getCartItemsLiveData();
            if (cartLiveData != null) {
                cartLiveData.observe(this, this::updateCartBadge);
            } else {
                cartManager.initializeForUser();
                cartManager.getCartItemsLiveData().observe(this, this::updateCartBadge);
            }
        } else {
            updateCartBadge(new ArrayList<>());
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
                filterAndSortProducts();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSortListener() {
        textSortBy.setOnClickListener(v -> {
            SortBottomSheetDialogFragment bottomSheet =
                    SortBottomSheetDialogFragment.newInstance(currentSortOptionId);
            bottomSheet.setSortListener(this);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void setupFilterListener() {
        filterIcon.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment bottomSheet =
                    FilterBottomSheetDialogFragment.newInstance(currentCategoryFilter, currentPriceRangeId);
            bottomSheet.setFilterListener(this);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
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
                filterAndSortProducts();
                productsRecyclerView.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(String message) {
                Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm: " + message, Toast.LENGTH_SHORT).show();
                productsRecyclerView.setVisibility(View.VISIBLE);
                filterAndSortProducts();
            }
        });
    }

    private void filterAndSortProducts() {
        if (currentFilterTask != null && currentFilterTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentFilterTask.cancel(true);
        }
        String query = searchInput.getText().toString().toLowerCase().trim();
        currentFilterTask = new FilterSortTask(query, currentCategoryFilter, currentPriceRangeId, currentSortOptionId);
        currentFilterTask.execute(new ArrayList<>(originalProductList));
    }

    // ⭐⭐⭐ BẮT ĐẦU SỬA (THÊM LOGIC SORT THEO TÊN) ⭐⭐⭐
    private class FilterSortTask extends AsyncTask<List<Product>, Void, List<Product>> {
        private String query;
        private String category;
        private int priceRangeId;
        private int sortId;

        FilterSortTask(String query, String category, int priceRangeId, int sortId) {
            this.query = query;
            this.category = category;
            this.priceRangeId = priceRangeId;
            this.sortId = sortId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Product> doInBackground(List<Product>... params) {
            List<Product> originalList = params[0];
            List<Product> filteredList;

            // 1. Lọc (Filter)
            if (query.isEmpty()) {
                filteredList = new ArrayList<>(originalList);
            } else {
                filteredList = originalList.stream()
                        .filter(product -> product.getName() != null && product.getName().toLowerCase().contains(query))
                        .collect(Collectors.toList());
            }

            if (!"Tất cả".equalsIgnoreCase(category)) {
                if (isCancelled()) return null;
                String categoryToFilter = category.toLowerCase();
                filteredList = filteredList.stream()
                        .filter(product -> product.getCategory() != null && product.getCategory().equalsIgnoreCase(categoryToFilter))
                        .collect(Collectors.toList());
            }

            if (isCancelled()) return null;
            if (priceRangeId == R.id.chipPrice1) {
                filteredList = filteredList.stream()
                        .filter(p -> p.getBasePrice() < 200000)
                        .collect(Collectors.toList());
            } else if (priceRangeId == R.id.chipPrice2) {
                filteredList = filteredList.stream()
                        .filter(p -> p.getBasePrice() >= 200000 && p.getBasePrice() <= 400000)
                        .collect(Collectors.toList());
            } else if (priceRangeId == R.id.chipPrice3) {
                filteredList = filteredList.stream()
                        .filter(p -> p.getBasePrice() > 400000)
                        .collect(Collectors.toList());
            }

            // 2. Sắp xếp (Sort)
            if (isCancelled()) return null;
            if (sortId == R.id.sort_price_asc) {
                // Sắp xếp Giá: Thấp đến Cao
                Collections.sort(filteredList, Comparator.comparingDouble(Product::getBasePrice));
            } else if (sortId == R.id.sort_price_desc) {
                // Sắp xếp Giá: Cao đến Thấp
                Collections.sort(filteredList, Comparator.comparingDouble(Product::getBasePrice).reversed());
            } else {
                // Mặc định (R.id.sort_default) -> Sắp xếp Theo tên A-Z
                Collections.sort(filteredList, Comparator.comparing(Product::getName));
            }

            return filteredList;
        }

        @Override
        protected void onPostExecute(List<Product> filteredList) {
            super.onPostExecute(filteredList);
            if (filteredList != null && productAdapter != null && !isCancelled()) {
                productAdapter.updateProducts(filteredList);
            }
        }
    }
    // ⭐⭐⭐ KẾT THÚC SỬA ⭐⭐⭐


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
                return true;
            } else if (itemId == R.id.nav_map) {
                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_orders) {
                Intent intent = new Intent(HomeActivity.this, MyOrdersActivity.class);
                startActivity(intent);
                return true;
            }
            else if (itemId == R.id.nav_chat) {
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onFilterApplied(String category, int priceRangeId) {
        this.currentCategoryFilter = category;
        this.currentPriceRangeId = priceRangeId;
        filterAndSortProducts();
    }

    @Override
    public void onSortApplied(int sortId, String title) {
        this.currentSortOptionId = sortId;
        // Cập nhật text hiển thị trên màn hình
        textSortBy.setText("Sắp xếp theo: " + title);
        filterAndSortProducts();
    }


    // (Hai hàm "nghe" Chat Badge giữ nguyên)
    private void setupChatBadgeListener() {
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            updateChatBadgeUI(0);
            return;
        }
        String currentUserId = currentUser.getUid();
        com.google.firebase.firestore.Query chatQuery = db.collection("Chats")
                .whereArrayContains("participants", currentUserId);
        chatListenerRegistration = chatQuery.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.w("HomeActivityChat", "Listen failed.", error);
                updateChatBadgeUI(0);
                return;
            }
            if (snapshots == null || snapshots.isEmpty()) {
                updateChatBadgeUI(0);
                return;
            }
            long totalUnread = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                List<String> participants = (List<String>) doc.get("participants");
                if (participants != null && participants.contains(SHOP_UID)) {
                    Long userUnreadCount = doc.getLong("userUnreadCount");
                    if (userUnreadCount != null) {
                        totalUnread = userUnreadCount;
                    }
                    break;
                }
            }
            updateChatBadgeUI((int) totalUnread);
        });
    }

    private void updateChatBadgeUI(int count) {
        if (bottomNavigation == null) return;
        BadgeDrawable badge = bottomNavigation.getOrCreateBadge(R.id.nav_chat);
        if (count > 0) {
            badge.setNumber(count);
            badge.setVisible(true);
        } else {
            badge.setVisible(false);
        }
    }
}