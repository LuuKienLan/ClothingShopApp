package com.example.clothingshopapp.ui.product;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.Product;
import com.example.clothingshopapp.data.model.Size;
import com.example.clothingshopapp.data.model.Variant;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.data.repository.ProductRepository;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productBrand, productPrice, quantityText, colorNameText, stockText;
    private Button buttonMinus, buttonPlus, addToCartButton;
    private ProgressBar detailProgressBar;
    private ConstraintLayout contentLayout;
    private LinearLayout colorSelectorLayout, sizeSelectorLayout;

    private int quantity = 1;
    private String selectedSize = null;
    private Product currentProduct;
    private Variant selectedVariant;
    private ProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productRepository = new ProductRepository();
        initViews();
        setupListeners();
        loadProductData();
    }

    private void initViews() {
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productBrand = findViewById(R.id.product_brand);
        productPrice = findViewById(R.id.product_price);
        quantityText = findViewById(R.id.quantity_text);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        detailProgressBar = findViewById(R.id.detailProgressBar);
        contentLayout = findViewById(R.id.contentLayout);
        colorNameText = findViewById(R.id.color_name_text);
        buttonMinus = findViewById(R.id.button_minus);
        buttonPlus = findViewById(R.id.button_plus);
        colorSelectorLayout = findViewById(R.id.color_options);
        sizeSelectorLayout = findViewById(R.id.size_options);
        stockText = findViewById(R.id.stock_text);
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
                if (currentProduct.getVariants() != null && !currentProduct.getVariants().isEmpty()) {
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
        setupColorOptions();
        updateForSelectedVariant();
    }

    private void setupColorOptions() {
        if (currentProduct.getVariants() == null) return;
        // 1. Dọn dẹp "cái khay", xóa hết các view cũ đi
        colorSelectorLayout.removeAllViews();
        // 2. Lặp qua danh sách màu từ Firebase
        for (Variant variant : currentProduct.getVariants()) {
            // 3. Tạo một vòng tròn cho mỗi màu và đặt vào "khay"
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
        updateMainImage();
        updateColorSelectionUI();
        updateColorNameText();
        setupSizeOptions();
    }

    private void setupSizeOptions() {
        sizeSelectorLayout.removeAllViews();
        selectedSize = null;
        stockText.setVisibility(View.INVISIBLE);

        if (selectedVariant == null || selectedVariant.getSizes() == null) return;

        for (int i = 0; i < selectedVariant.getSizes().size(); i++) {
            Size size = selectedVariant.getSizes().get(i);
            TextView sizeView = createSizeView(this, size);
            sizeSelectorLayout.addView(sizeView);
            // Tự động chọn size đầu tiên
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

    private void updateMainImage() {
        if (selectedVariant != null) {
            Glide.with(this).load(selectedVariant.getImageUrl()).placeholder(R.color.gray_icon).into(productImage);
        }
    }

    private void updateColorSelectionUI() {
        for (int i = 0; i < colorSelectorLayout.getChildCount(); i++) {
            ImageView colorView = (ImageView) colorSelectorLayout.getChildAt(i);
            Variant variant = currentProduct.getVariants().get(i);
            if (selectedVariant != null && selectedVariant.getColor().equals(variant.getColor())) {
                colorView.setForeground(ContextCompat.getDrawable(this, R.drawable.color_selector_border));
            } else {
                colorView.setForeground(null);
            }
        }
    }

    private void updateColorNameText() {
        if (selectedVariant != null) {
            colorNameText.setText("Màu sắc: " + selectedVariant.getColor());
        }
    }

    private void addToCart() {
        if (currentProduct == null || selectedVariant == null) {
            Toast.makeText(this, "Vui lòng chọn màu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSize == null) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return;
        }
        CartItem newItem = new CartItem(currentProduct, selectedVariant, quantity, selectedSize);
        CartManager.getInstance().addItem(newItem);
        Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
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

    private void setupListeners() {
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
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
        addToCartButton.setOnClickListener(v -> addToCart());
    }
}