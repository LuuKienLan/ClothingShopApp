// File: ui/product/ProductDetailActivity.java
package com.example.clothingshopapp.ui.product;

import android.content.Intent; // THÊM DÒNG NÀY
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.CartManager;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.ui.cart.CartActivity; // THÊM DÒNG NÀY

public class ProductDetailActivity extends AppCompatActivity {

    // Khai báo các biến cho view
    private ImageView backArrow, productImage;
    private TextView productName, productBrand, productPrice, quantityText;
    private Button buttonMinus, buttonPlus, addToCartButton;

    // Biến để lưu trạng thái người dùng chọn
    private int quantity = 1;
    private String selectedColor = "Black"; // Mặc định
    private String selectedSize = "S";      // Mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        setupListeners();
        loadProductData();
    }

    private void initViews() {
        backArrow = findViewById(R.id.back_arrow);
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productBrand = findViewById(R.id.product_brand);
        productPrice = findViewById(R.id.product_price);
        quantityText = findViewById(R.id.quantity_text);
        buttonMinus = findViewById(R.id.button_minus);
        buttonPlus = findViewById(R.id.button_plus);
        addToCartButton = findViewById(R.id.add_to_cart_button);
    }

    private void setupListeners() {
        backArrow.setOnClickListener(v -> finish());

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

        addToCartButton.setOnClickListener(v -> {
            addToCart();
        });
    }

    private void loadProductData() {
        productName.setText("Cotton queen T-shirt");
        productBrand.setText("Siz sigma");
        productPrice.setText("$43.00");
        productImage.setImageResource(R.drawable.mock_tshirt_main);
        quantityText.setText(String.valueOf(quantity));
    }

    /**
     * Logic xử lý khi người dùng nhấn nút "Add to Cart".
     */
    private void addToCart() {
        // 1. Lấy thông tin sản phẩm từ giao diện
        String name = productName.getText().toString();
        double price = Double.parseDouble(productPrice.getText().toString().replace("$", ""));

        // 2. Tạo đối tượng CartItem mới
        CartItem newItem = new CartItem(name, price, selectedSize, quantity, R.drawable.mock_tshirt_main);

        // 3. Thêm vào CartManager - nguồn dữ liệu duy nhất
        CartManager.getInstance().addItem(newItem);

        // 4. Hiển thị thông báo cho người dùng
        Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();

        // 5. SỬA ĐỔI QUAN TRỌNG: Chuyển sang màn hình giỏ hàng
        Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
        startActivity(intent);
    }
}