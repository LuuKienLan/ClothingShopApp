package com.example.clothingshopapp.ui.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Product;
import com.example.clothingshopapp.data.model.Size;
import com.example.clothingshopapp.data.model.Variant;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.data.repository.ProductRepository;
import com.example.clothingshopapp.ui.adapter.ImageSliderAdapter;
import com.example.clothingshopapp.ui.adapter.ThumbnailAdapter;
import com.example.clothingshopapp.ui.cart.CartActivity;
import com.example.clothingshopapp.ui.cart.CheckoutActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity implements ThumbnailAdapter.OnThumbnailClickListener {

    private ViewPager2 productImageSlider;
    private RecyclerView thumbnailRecyclerView;

    private TextView productName, productBrand, productPrice, quantityText, colorNameText, stockText, productDescription;
    private Button buttonMinus, buttonPlus;
    private LinearLayout colorSelectorLayout, sizeSelectorLayout;

    private Button addToCartButton, buyNowButton;
    private ProgressBar detailProgressBar;
    private ConstraintLayout contentLayout;

    private FrameLayout cartContainer;
    private TextView cartBadge;

    private ImageSliderAdapter imageSliderAdapter;
    private ThumbnailAdapter thumbnailAdapter;

    private int quantity = 1;
    private String selectedSize = null;

    private Product currentProduct;
    private Variant selectedVariant;

    private ProductRepository productRepository;

    private boolean isUpdatingFromPager = false;
    private boolean isUpdatingFromThumbnail = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productRepository = new ProductRepository();
        initViews();
        setupListeners();
        loadProductData();
        observeCartChanges();
    }

    private void initViews() {
        productImageSlider = findViewById(R.id.product_image_slider);
        thumbnailRecyclerView = findViewById(R.id.thumbnail_recycler_view);

        productName = findViewById(R.id.product_name);
        productBrand = findViewById(R.id.product_brand);
        productPrice = findViewById(R.id.product_price);
        quantityText = findViewById(R.id.quantity_text);
        colorNameText = findViewById(R.id.color_name_text);
        buttonMinus = findViewById(R.id.button_minus);
        buttonPlus = findViewById(R.id.button_plus);
        colorSelectorLayout = findViewById(R.id.color_options);
        sizeSelectorLayout = findViewById(R.id.size_options);
        stockText = findViewById(R.id.stock_text);
        productDescription = findViewById(R.id.productDescription);

        addToCartButton = findViewById(R.id.add_to_cart_button);
        buyNowButton = findViewById(R.id.buyNowButton);
        detailProgressBar = findViewById(R.id.detailProgressBar);
        contentLayout = findViewById(R.id.contentLayout);

        cartContainer = findViewById(R.id.cartContainer);
        cartBadge = findViewById(R.id.cartBadge); // Đã sửa lỗi (R.id.cartBadge)
    }

    private void setupListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        cartContainer.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
        buttonMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });
        buttonPlus.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });
        addToCartButton.setOnClickListener(v -> handleAddToCart());
        buyNowButton.setOnClickListener(v -> handleBuyNow());
    }

    private void loadProductData() {
        String productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Thiếu ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        detailProgressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.INVISIBLE);
        productRepository.getProductById(productId, new ProductRepository.SingleProductCallback() {
            @Override
            public void onProductLoaded(Product product) {
                currentProduct = product;
                if (currentProduct != null && currentProduct.getVariants() != null && !currentProduct.getVariants().isEmpty()) {
                    selectedVariant = currentProduct.getVariants().get(0);
                }
                displayProductDetails();
                detailProgressBar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onError(String message) {
                detailProgressBar.setVisibility(View.GONE);
                Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayProductDetails() {
        if (currentProduct == null) return;
        productName.setText(currentProduct.getName());
        productBrand.setText("Clothing Shop");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        productPrice.setText(formatter.format(currentProduct.getBasePrice()));
        quantityText.setText(String.valueOf(quantity));
        productDescription.setText(currentProduct.getDescription());
        setupColorOptions();
        updateForSelectedVariant();
    }

    private void setupColorOptions() {
        if (currentProduct.getVariants() == null) return;
        colorSelectorLayout.removeAllViews();
        for (Variant variant : currentProduct.getVariants()) {
            colorSelectorLayout.addView(createColorView(this, variant));
        }
    }

    private ImageView createColorView(Context context, Variant variant) {
        ImageView colorView = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
        params.setMargins(0, 0, dpToPx(8), 0);
        colorView.setLayoutParams(params);
        colorView.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor(getHexColor(variant.getColor())));
        if (variant.getColor().equalsIgnoreCase("Trắng")) {
            shape.setStroke(dpToPx(1), Color.LTGRAY);
        }
        colorView.setBackground(shape);
        colorView.setOnClickListener(v -> {
            selectedVariant = variant;
            updateForSelectedVariant();
        });
        return colorView;
    }

    private void updateForSelectedVariant() {
        if (selectedVariant == null) return;
        updateMainImageSlider();
        updateColorSelectionUI();
        updateColorNameText();
        setupSizeOptions();
    }

    private void setupSizeOptions() {
        sizeSelectorLayout.removeAllViews();
        selectedSize = null; // Reset
        stockText.setVisibility(View.INVISIBLE);

        if (selectedVariant == null || selectedVariant.getSizes() == null) return;

        for (int i = 0; i < selectedVariant.getSizes().size(); i++) {
            Size size = selectedVariant.getSizes().get(i);
            TextView sizeView = createSizeView(this, size);
            sizeSelectorLayout.addView(sizeView);

            if (i == 0) {
                sizeView.performClick();
            }
        }
    }

    private TextView createSizeView(Context context, Size size) {
        TextView sizeView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(40));
        params.setMargins(dpToPx(8), 0, 0, 0);
        sizeView.setLayoutParams(params);
        sizeView.setText(size.getSize());
        sizeView.setGravity(Gravity.CENTER);
        sizeView.setTextColor(Color.BLACK);
        sizeView.setBackgroundResource(R.drawable.size_selector_default);
        sizeView.setOnClickListener(v -> {
            selectedSize = size.getSize();
            updateSizeSelectionUI();
            updateStockDisplay();
        });
        return sizeView;
    }

    private void updateSizeSelectionUI() {
        for (int i = 0; i < sizeSelectorLayout.getChildCount(); i++) {
            TextView sizeView = (TextView) sizeSelectorLayout.getChildAt(i);
            if (sizeView.getText().toString().equals(selectedSize)) {
                sizeView.setBackgroundResource(R.drawable.size_selector_selected);
                sizeView.setTextColor(Color.WHITE);
            } else {
                sizeView.setBackgroundResource(R.drawable.size_selector_default);
                sizeView.setTextColor(Color.BLACK);
            }
        }
    }
    private void updateStockDisplay() {
        if (selectedVariant != null && selectedVariant.getSizes() != null && selectedSize != null) {
            for (Size size : selectedVariant.getSizes()) {
                if (size.getSize().equals(selectedSize)) {
                    stockText.setText("Còn lại: " + size.getStock());
                    stockText.setVisibility(View.VISIBLE);
                    return;
                }
            }
        }
        stockText.setVisibility(View.INVISIBLE);
    }

    private void updateMainImageSlider() {
        if (selectedVariant == null || selectedVariant.getImageUrls() == null) return;
        List<String> images = selectedVariant.getImageUrls();
        imageSliderAdapter = new ImageSliderAdapter(this, images);
        productImageSlider.setAdapter(imageSliderAdapter);
        thumbnailAdapter = new ThumbnailAdapter(this, images, this);
        thumbnailRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        thumbnailRecyclerView.setAdapter(thumbnailAdapter);
        if (images.size() <= 1) {
            thumbnailRecyclerView.setVisibility(View.GONE);
        } else {
            thumbnailRecyclerView.setVisibility(View.VISIBLE);
        }
        productImageSlider.unregisterOnPageChangeCallback(pagerCallback);
        productImageSlider.registerOnPageChangeCallback(pagerCallback);
    }

    private ViewPager2.OnPageChangeCallback pagerCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (isUpdatingFromThumbnail) return;
            isUpdatingFromPager = true;
            if (thumbnailAdapter != null) {
                thumbnailAdapter.setSelectedPosition(position);
                thumbnailRecyclerView.smoothScrollToPosition(position);
            }
            isUpdatingFromPager = false;
        }
    };

    @Override
    public void onThumbnailClick(int position) {
        if (isUpdatingFromPager) return;
        isUpdatingFromThumbnail = true;
        productImageSlider.setCurrentItem(position, true);
        thumbnailAdapter.setSelectedPosition(position);
        isUpdatingFromThumbnail = false;
    }

    private void updateColorSelectionUI() {
        if (currentProduct.getVariants() == null) return;
        for (int i = 0; i < colorSelectorLayout.getChildCount(); i++) {
            if (i < currentProduct.getVariants().size()) {
                ImageView colorView = (ImageView) colorSelectorLayout.getChildAt(i);
                Variant variant = currentProduct.getVariants().get(i);
                if (selectedVariant != null && selectedVariant.getColor().equals(variant.getColor())) {
                    colorView.setForeground(ContextCompat.getDrawable(this, R.drawable.color_selector_border));
                } else {
                    colorView.setForeground(null);
                }
            }
        }
    }
    private void updateColorNameText() {
        if (selectedVariant != null) {
            colorNameText.setText("Màu sắc: " + selectedVariant.getColor());
        }
    }

    // ⭐ SỬA LỖI: Quay lại dùng constructor 4-tham-số
    private CartItem validateAndCreateItem() {
        if (currentProduct == null || selectedVariant == null) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (selectedSize == null) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return null;
        }
        // Gọi constructor 4-tham-số (bản gốc)
        return new CartItem(currentProduct, selectedVariant, quantity, selectedSize);
    }

    private void handleAddToCart() {
        CartItem newItem = validateAndCreateItem();
        if (newItem == null) return;
        CartManager.getInstance().addItem(newItem);
        Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
    }
    private void handleBuyNow() {
        CartItem newItem = validateAndCreateItem();
        if (newItem == null) return;
        ArrayList<CartItem> buyNowList = new ArrayList<>();
        buyNowList.add(newItem);
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("CHECKOUT_ITEMS", (Serializable) buyNowList);
        intent.putExtra("IS_BUY_NOW", true);
        startActivity(intent);
    }
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    private String getHexColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "trắng": return "#FFFFFF";
            case "đen": return "#000000";
            case "xám": return "#A9A9A9";
            default: return "#CCCCCC";
        }
    }
    private void observeCartChanges() {
        CartManager cartManager = CartManager.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            LiveData<List<CartItem>> cartLiveData = cartManager.getCartItemsLiveData();
            if (cartLiveData != null) {
                cartLiveData.observe(this, this::updateCartBadge);
            }
        } else {
            updateCartBadge(new ArrayList<>());
        }
    }
    private void updateCartBadge(List<CartItem> cartItems) {
        if (cartItems == null || cartBadge == null) return;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productImageSlider != null) {
            productImageSlider.unregisterOnPageChangeCallback(pagerCallback);
        }
    }
}