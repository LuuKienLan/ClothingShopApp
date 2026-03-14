package com.example.clothingshopapp.ui.cart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.remote.CartManager;
import com.example.clothingshopapp.ui.product.HomeActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không cần giao diện, chỉ xử lý logic và chuyển đi

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            String orderId = data.getQueryParameter("vnp_TxnRef"); // Lấy lại OrderId
            String totalAmountStr = data.getQueryParameter("vnp_Amount");

            if (orderId == null) {
                Log.e(TAG, "OrderId bị null từ VNPAY return URL");
                goToHome("Thanh toán có lỗi, OrderId không tồn tại.");
                return;
            }

            if ("00".equals(responseCode)) {
                // Thanh toán thành công ("00" là mã thành công của VNPAY)
                Log.d(TAG, "Thanh toán VNPAY thành công cho đơn: " + orderId);
                double totalAmount = Double.parseDouble(totalAmountStr) / 100;

                // Cập nhật trạng thái đơn hàng trên Firebase
                updateOrderStatus(orderId, "Pending", totalAmount);
            } else {
                // Thanh toán thất bại (người dùng hủy, sai OTP, v.v.)
                Log.w(TAG, "Thanh toán VNPAY thất bại cho đơn: " + orderId + ", Code: " + responseCode);

                // Cập nhật trạng thái thất bại (hoặc xóa đơn hàng)
                updateOrderStatus(orderId, "PaymentFailed", 0);
            }
        } else {
            Log.e(TAG, "Intent data is null, không thể xử lý kết quả VNPAY");
            goToHome("Không nhận được dữ liệu thanh toán.");
        }
    }

    private void updateOrderStatus(String orderId, String status, double totalAmount) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        // Cập nhật trạng thái
        orderRef.child("status").setValue(status);

        if ("Pending".equals(status)) {
            // Nếu thành công:
            orderRef.child("totalAmount").setValue(totalAmount);
            orderRef.child("paymentMethod").setValue("VNPAY");
            orderRef.child("timestamp").setValue(System.currentTimeMillis());

            // Xóa giỏ hàng
            CartManager.getInstance().clearCart();

            // Chuyển đến màn hình "Đặt hàng thành công"
            Intent successIntent = new Intent(this, OrderConfirmationActivity.class);
            successIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(successIntent);
        } else {
            // Nếu thất bại, quay về Home
            goToHome("Thanh toán thất bại. Vui lòng thử lại.");
        }
        finish(); // Đóng Activity này
    }
    private void goToHome(String toastMessage) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeIntent);
        finish();
    }
}