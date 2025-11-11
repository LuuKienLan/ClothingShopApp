package com.example.clothingshopapp.data.remote;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.product.HomeActivity; // Sửa path nếu HomeActivity của bạn ở chỗ khác

public class NotificationHelper {

    private static final String CHANNEL_ID = "CART_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 1122; // Một ID cố định
    private Context context;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    /**
     * Tạo Notification Channel (Bắt buộc cho Android 8.0 Oreo trở lên)
     */
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Giỏ hàng", // Tên người dùng thấy
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo về số lượng giỏ hàng");
            channel.setShowBadge(true); // QUAN TRỌNG: Cho phép kênh này hiển thị badge
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Hiển thị thông báo (và badge)
     */
    public void showNotification(int itemCount) {
        // Tạo Intent để khi click vào thông báo sẽ mở lại HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Tạo thông báo
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cart_nav) // ⭐ BẮT BUỘC (Xem Bước 3)
                .setContentTitle("Giỏ hàng của bạn")
                .setContentText("Bạn có " + itemCount + " sản phẩm đang chờ.")
                .setNumber(itemCount) // ⭐ SỐ HIỂN THỊ TRÊN BADGE
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(pendingIntent) // Intent khi click
                .setOnlyAlertOnce(true) // Chỉ kêu 1 lần
                .setAutoCancel(true) // Tự hủy khi người dùng click
                .build();

        // Kiểm tra quyền POST_NOTIFICATIONS (cho Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa có quyền, chúng ta không thể gửi (sẽ xin quyền ở HomeActivity)
                return;
            }
        }

        // Gửi thông báo
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Hủy thông báo (khi mở app hoặc giỏ hàng trống)
     */
    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}