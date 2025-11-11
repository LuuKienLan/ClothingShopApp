package com.example.clothingshopapp.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.clothingshopapp.data.model.CartItem;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.data.remote.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class    MainApplication extends Application implements LifecycleEventObserver {

    private NotificationHelper notificationHelper;
    private boolean isAppInForeground = false;
    private int latestCartCount = 0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private LiveData<List<CartItem>> cartLiveData;
    private Observer<List<CartItem>> cartObserver;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Khởi tạo NotificationHelper và tạo Channel
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();

        // 2. Theo dõi trạng thái App (Foreground/Background)
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // 3. Chuẩn bị hàm Observer giỏ hàng
        cartObserver = cartItems -> {
            latestCartCount = 0;
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    latestCartCount += item.getQuantity();
                }
            }
            updateNotificationBadge(); // Cập nhật mỗi khi giỏ hàng thay đổi
        };

        // 4. Theo dõi trạng thái Đăng nhập
        mAuth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User Đăng nhập
                CartManager.getInstance().initializeForUser();
                cartLiveData = CartManager.getInstance().getCartItemsLiveData();

                // Dùng ProcessLifecycleOwner để tự động quản lý vòng đời observer
                cartLiveData.observe(ProcessLifecycleOwner.get(), cartObserver);
            } else {
                // User Đăng xuất
                if (cartLiveData != null) {
                    cartLiveData.removeObserver(cartObserver);
                }
                CartManager.getInstance().destroyInstance();
                latestCartCount = 0;
                updateNotificationBadge(); // Xóa badge khi đăng xuất
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }

    /**
     * Được gọi khi App chuyển sang Foreground hoặc Background
     */
    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_START) {
            isAppInForeground = true;
            updateNotificationBadge();
        } else if (event == Lifecycle.Event.ON_STOP) {
            isAppInForeground = false;
            updateNotificationBadge();
        }
    }

    /**
     * Logic chính: Hiển thị/Ẩn badge
     */
    private void updateNotificationBadge() {
        if (isAppInForeground) {
            // Nếu App đang mở, HỦY thông báo
            notificationHelper.cancelNotification();
        } else {
            // Nếu App đang chạy nền
            if (latestCartCount > 0) {
                // HIỆN thông báo (và badge)
                notificationHelper.showNotification(latestCartCount);
            } else {
                // Nếu giỏ hàng trống, HỦY thông báo
                notificationHelper.cancelNotification();
            }
        }
    }
}