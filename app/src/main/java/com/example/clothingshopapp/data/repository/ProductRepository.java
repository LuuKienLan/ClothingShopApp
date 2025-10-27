package com.example.clothingshopapp.data.repository;

import androidx.annotation.NonNull;
import com.example.clothingshopapp.data.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final DatabaseReference databaseReference;

    public ProductRepository() {
        // Trỏ đến nút "products" trong Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
    }

    public interface ProductListCallback {
        void onProductsLoaded(List<Product> products);
        void onError(String message);
    }

    public interface SingleProductCallback {
        void onProductLoaded(Product product);
        void onError(String message);
    }

    public void getAllProducts(ProductListCallback callback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> productList = new ArrayList<>();

                // ⭐ ĐIỂM SỬA QUAN TRỌNG NHẤT LÀ ĐÂY ⭐
                // Duyệt qua tất cả các con của nút "products" (tức là SP001, SP002...)
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Chuyển đổi dữ liệu của TỪNG sản phẩm thành đối tượng Product
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        // Lấy key (ví dụ: "SP001") và gán vào cho đối tượng
                        product.setProductId(productSnapshot.getKey());
                        productList.add(product);
                    }
                }
                callback.onProductsLoaded(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getProductById(String productId, SingleProductCallback callback) {
        // Trỏ trực tiếp đến sản phẩm con bằng ID
        databaseReference.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setProductId(snapshot.getKey());
                    callback.onProductLoaded(product);
                } else {
                    callback.onError("Không tìm thấy sản phẩm");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}