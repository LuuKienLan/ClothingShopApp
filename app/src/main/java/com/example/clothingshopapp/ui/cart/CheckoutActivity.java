package com.example.clothingshopapp.ui.cart;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.clothingshopapp.R;

public class CheckoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Lấy các view
        CardView addressCard1 = findViewById(R.id.address_card_1);
        CardView addressCard2 = findViewById(R.id.address_card_2);
        TextView addNewAddressText = findViewById(R.id.add_new_address_text);
        Button payButton = findViewById(R.id.pay_button);

        // Xử lý sự kiện click
        findViewById(R.id.back_arrow).setOnClickListener(v -> finish());

        addNewAddressText.setOnClickListener(v -> {
            // Tạm thời chỉ hiển thị thông báo
            // Sau này sẽ mở ra một màn hình/dialog để thêm địa chỉ mới
            Toast.makeText(this, "Chuyển đến màn hình thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
        });

        payButton.setOnClickListener(v -> {
            // Tạm thời chỉ hiển thị thông báo
            // Sau này sẽ xử lý logic thanh toán và tạo đơn hàng
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
        });

        // Logic để chọn địa chỉ (chỉ là giả lập)
        addressCard1.setOnClickListener(v -> {
            // Logic để đổi màu và trạng thái của card
            Toast.makeText(this, "Address 1 selected", Toast.LENGTH_SHORT).show();
        });

        addressCard2.setOnClickListener(v -> {
            Toast.makeText(this, "Address 2 selected", Toast.LENGTH_SHORT).show();
        });
    }
}