package com.example.clothingshopapp.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.auth.ProfileActivity;
import com.example.clothingshopapp.ui.notification.NotificationActivity;
import com.example.clothingshopapp.ui.orders.MyOrdersActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProductViewModel extends AppCompatActivity {

    private TextView greetingText, cartBadge;
    private EditText searchInput;
    private ImageView cartIcon;
    private BottomNavigationView bottomNavigation;

    // Product favorite cards
    private CardView product1Card, product2Card, product3Card, product4Card;
    private CardView product1Favorite, product2Favorite, product3Favorite, product4Favorite;

    private int cartCount = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupUserGreeting();
        setupClickListeners();
        updateCartBadge();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greetingText);
        cartBadge = findViewById(R.id.cartBadge);
        searchInput = findViewById(R.id.searchInput);
        cartIcon = findViewById(R.id.cartIcon);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Product cards (for click to detail)
        product1Card = findViewById(R.id.product1Card);
        product2Card = findViewById(R.id.product2Card);
        product3Card = findViewById(R.id.product3Card);
        product4Card = findViewById(R.id.product4Card);

        // Product favorites
        product1Favorite = findViewById(R.id.product1Favorite);
        product2Favorite = findViewById(R.id.product2Favorite);
        product3Favorite = findViewById(R.id.product3Favorite);
        product4Favorite = findViewById(R.id.product4Favorite);
    }

    private void setupUserGreeting() {
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null && userEmail.contains("@")) {
            String name = userEmail.split("@")[0];
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            greetingText.setText("Hi, " + name + "!");
        } else {
            greetingText.setText("Hi, Selina!");
        }
    }

    private void setupClickListeners() {
        // Cart icon
        cartIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Giỏ hàng: " + cartCount + " sản phẩm", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to CartActivity
        });

        // Product cards - Navigate to detail
        product1Card.setOnClickListener(v -> openProductDetail(1, "Cotton queen T", "$43.00"));
        product2Card.setOnClickListener(v -> openProductDetail(2, "Cotton Style T", "$40.50"));
        product3Card.setOnClickListener(v -> openProductDetail(3, "White Plain T", "$35.00"));
        product4Card.setOnClickListener(v -> openProductDetail(4, "Blue Classic T", "$38.00"));

        // Favorite buttons
        setupFavoriteButton(product1Favorite, "Cotton queen T");
        setupFavoriteButton(product2Favorite, "Cotton Style T");
        setupFavoriteButton(product3Favorite, "White Plain T");
        setupFavoriteButton(product4Favorite, "Blue Classic T");

        // Bottom navigation
        setupBottomNavigation();

        // Search
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                Toast.makeText(this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void openProductDetail(int productId, String productName, String price) {
        Intent intent = new Intent(ProductViewModel.this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", productId);
        intent.putExtra("PRODUCT_NAME", productName);
        intent.putExtra("PRODUCT_PRICE", price);
        startActivity(intent);
    }

    private void setupFavoriteButton(CardView favoriteCard, String productName) {
        final boolean[] isFavorite = {false};

        favoriteCard.setOnClickListener(v -> {
            isFavorite[0] = !isFavorite[0];

            ImageView heartIcon = (ImageView) favoriteCard.getChildAt(0);

            if (isFavorite[0]) {
                heartIcon.setImageResource(R.drawable.ic_heart_filled);
                Toast.makeText(this,
                        "Đã thêm " + productName + " vào yêu thích",
                        Toast.LENGTH_SHORT).show();
            } else {
                heartIcon.setImageResource(R.drawable.ic_heart_outline);
                Toast.makeText(this,
                        "Đã xóa " + productName + " khỏi yêu thích",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Intent intent = new Intent(ProductViewModel.this, NotificationActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_orders) {
                // Chuyển sang màn hình orders
                Intent intent = new Intent(ProductViewModel.this, MyOrdersActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(ProductViewModel.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void updateCartBadge() {
        if (cartCount > 0) {
            cartBadge.setText(String.valueOf(cartCount));
            cartBadge.setVisibility(android.view.View.VISIBLE);
        } else {
            cartBadge.setVisibility(android.view.View.GONE);
        }
    }
}