package com.example.clothingshopapp.data.remote;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.model.FirebaseCartItem;
import com.example.clothingshopapp.data.model.Product;
import com.example.clothingshopapp.data.model.Variant;
import com.example.clothingshopapp.data.repository.ProductRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CartManager {

    private static final String TAG = "CartManager";
    private static CartManager instance;
    private DatabaseReference userCartDbReference;
    private ValueEventListener cartEventListener;
    private final ProductRepository productRepository;
    private final MutableLiveData<List<CartItem>> cartItemsLiveData = new MutableLiveData<>();

    private CartManager() {
        productRepository = new ProductRepository();
        cartItemsLiveData.setValue(new ArrayList<>());
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public LiveData<List<CartItem>> getCartItemsLiveData() {
        return cartItemsLiveData;
    }

    public void initializeForUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userCartDbReference = FirebaseDatabase.getInstance().getReference("carts").child(uid);
            attachCartDatabaseListener();
        }
    }

    private void attachCartDatabaseListener() {
        if (userCartDbReference == null) return;
        if (cartEventListener != null) userCartDbReference.removeEventListener(cartEventListener);

        cartEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FirebaseCartItem> firebaseItems = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FirebaseCartItem fbItem = snapshot.getValue(FirebaseCartItem.class);
                    if (fbItem != null) {
                        fbItem.setFirebaseKey(snapshot.getKey());
                        firebaseItems.add(fbItem);
                    }
                }
                loadFullProductDetails(firebaseItems);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read cart data.", databaseError.toException());
            }
        };
        userCartDbReference.addValueEventListener(cartEventListener);
    }

    private void loadFullProductDetails(List<FirebaseCartItem> firebaseItems) {
        List<CartItem> fullCartItems = new ArrayList<>();
        if (firebaseItems.isEmpty()) {
            cartItemsLiveData.postValue(fullCartItems);
            return;
        }

        AtomicInteger itemsToLoad = new AtomicInteger(firebaseItems.size());
        for (FirebaseCartItem fbItem : firebaseItems) {
            productRepository.getProductById(fbItem.getProductId(), new ProductRepository.SingleProductCallback() {
                @Override
                public void onProductLoaded(Product product) {
                    Variant selectedVariant = findVariantByColor(product, fbItem.getColor());
                    if (selectedVariant != null) {
                        CartItem cartItem = new CartItem(product, selectedVariant, fbItem.getQuantity(), fbItem.getSize());
                        cartItem.setFirebaseKey(fbItem.getFirebaseKey());
                        fullCartItems.add(cartItem);
                    }
                    if (itemsToLoad.decrementAndGet() == 0) {
                        cartItemsLiveData.postValue(fullCartItems);
                    }
                }
                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error loading product " + fbItem.getProductId() + ": " + message);
                    if (itemsToLoad.decrementAndGet() == 0) {
                        cartItemsLiveData.postValue(fullCartItems);
                    }
                }
            });
        }
    }

    private Variant findVariantByColor(Product product, String color) {
        if (product.getVariants() != null) {
            for (Variant v : product.getVariants()) {
                if (v.getColor().equals(color)) {
                    return v;
                }
            }
        }
        return null;
    }

    // ⭐ BẮT ĐẦU PHẦN SỬA LỖI TẠI ĐÂY ⭐
    public void addItem(CartItem newItem) {
        if (userCartDbReference == null) {
            Log.e(TAG, "Cannot add item, userCartDbReference is null. Is user logged in?");
            return;
        }

        // Lấy danh sách item hiện tại từ LiveData để kiểm tra trùng lặp
        List<CartItem> currentItems = cartItemsLiveData.getValue();
        if (currentItems != null) {
            for (CartItem existingItem : currentItems) {
                boolean sameProduct = existingItem.getProduct().getProductId().equals(newItem.getProduct().getProductId());
                boolean sameColor = existingItem.getVariant().getColor().equals(newItem.getVariant().getColor());
                boolean sameSize = existingItem.getSize().equals(newItem.getSize());

                if (sameProduct && sameColor && sameSize) {
                    // Nếu đã tồn tại, cập nhật số lượng
                    int newQuantity = existingItem.getQuantity() + newItem.getQuantity();
                    updateQuantity(existingItem, newQuantity);
                    return; // Thoát khỏi hàm
                }
            }
        }

        // Nếu là item mới, tạo đối tượng FirebaseCartItem để đẩy lên Firebase
        FirebaseCartItem fbItem = new FirebaseCartItem(
                newItem.getProduct().getProductId(),
                newItem.getVariant().getColor(),
                newItem.getSize(),
                newItem.getQuantity()
        );
        userCartDbReference.push().setValue(fbItem);
    }
    // ⭐ KẾT THÚC PHẦN SỬA LỖI ⭐

    public void removeItem(CartItem itemToRemove) {
        if (userCartDbReference != null && itemToRemove.getFirebaseKey() != null) {
            userCartDbReference.child(itemToRemove.getFirebaseKey()).removeValue();
        }
    }

    public void updateQuantity(CartItem itemToUpdate, int newQuantity) {
        if (userCartDbReference != null && itemToUpdate.getFirebaseKey() != null) {
            if (newQuantity <= 0) {
                removeItem(itemToUpdate);
            } else {
                userCartDbReference.child(itemToUpdate.getFirebaseKey()).child("quantity").setValue(newQuantity);
            }
        }
    }

    public void clearCart() {
        if (userCartDbReference != null) {
            userCartDbReference.removeValue();
        }
    }

    public void destroyInstance() {
        if (userCartDbReference != null && cartEventListener != null) {
            userCartDbReference.removeEventListener(cartEventListener);
        }
        cartItemsLiveData.setValue(new ArrayList<>());
        userCartDbReference = null;
        instance = null;
    }


    public double getSubtotal() {
        List<CartItem> currentItems = cartItemsLiveData.getValue();
        if (currentItems == null) {
            return 0.0;
        }

        double subtotal = 0.0;
        for (CartItem item : currentItems) {
            subtotal += item.getProduct().getBasePrice() * item.getQuantity();
        }
        return subtotal;
    }
}