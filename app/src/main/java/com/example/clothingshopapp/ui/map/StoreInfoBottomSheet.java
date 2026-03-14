package com.example.clothingshopapp.ui.map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.clothingshopapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.osmdroid.util.GeoPoint; // ⭐ Thêm import
import java.util.Locale;

public class StoreInfoBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "StoreInfoBottomSheet";

    // --- Argument keys (giữ nguyên) ---
    private static final String ARG_STORE_NAME = "storeName";
    private static final String ARG_STORE_ADDRESS = "storeAddress";
    private static final String ARG_STORE_HOURS = "storeHours";
    private static final String ARG_STORE_PHONE = "storePhone";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_CURRENT_LAT = "currentLat";
    private static final String ARG_CURRENT_LNG = "currentLng";

    public static StoreInfoBottomSheet newInstance(String storeName, String storeAddress, String storeHours, String storePhone,
                                                   double storeLatitude, double storeLongitude,
                                                   Double currentLat, Double currentLng) {
        // ... (Code hàm newInstance giữ nguyên) ...
        StoreInfoBottomSheet fragment = new StoreInfoBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_NAME, storeName);
        args.putString(ARG_STORE_ADDRESS, storeAddress);
        args.putString(ARG_STORE_HOURS, storeHours);
        args.putString(ARG_STORE_PHONE, storePhone);
        args.putDouble(ARG_LATITUDE, storeLatitude);
        args.putDouble(ARG_LONGITUDE, storeLongitude);
        if (currentLat != null) args.putDouble(ARG_CURRENT_LAT, currentLat);
        if (currentLng != null) args.putDouble(ARG_CURRENT_LNG, currentLng);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_store_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ... (Ánh xạ các view giữ nguyên) ...
        TextView storeName = view.findViewById(R.id.storeName);
        TextView storeAddress = view.findViewById(R.id.storeAddress);
        TextView storeHours = view.findViewById(R.id.storeHours);
        TextView storePhone = view.findViewById(R.id.storePhone);
        Button btnGetDirections = view.findViewById(R.id.btnGetDirections);
        Button btnCallStore = view.findViewById(R.id.btnCallStore);

        Bundle args = getArguments();
        if (args != null) {
            // ... (Lấy dữ liệu từ args giữ nguyên) ...
            String name = args.getString(ARG_STORE_NAME);
            String address = args.getString(ARG_STORE_ADDRESS);
            String hours = args.getString(ARG_STORE_HOURS);
            String phone = args.getString(ARG_STORE_PHONE);
            double storeLat = args.getDouble(ARG_LATITUDE);
            double storeLng = args.getDouble(ARG_LONGITUDE);
            Double currentLat = args.containsKey(ARG_CURRENT_LAT) ? args.getDouble(ARG_CURRENT_LAT) : null;
            Double currentLng = args.containsKey(ARG_CURRENT_LNG) ? args.getDouble(ARG_CURRENT_LNG) : null;

            // ... (Gán dữ liệu cho text view giữ nguyên) ...
            storeName.setText(name);
            storeAddress.setText(address);
            storeHours.setText(hours);
            storePhone.setText(phone);


            // ⭐ SỬA LẠI LOGIC NÚT CHỈ ĐƯỜNG ĐỂ VẼ TRÊN MAP ⭐
            btnGetDirections.setOnClickListener(v -> {
                if (currentLat != null && currentLng != null) {
                    // Kiểm tra xem Activity có phải là MapActivity không
                    if (getActivity() instanceof MapActivity) {
                        // Gọi hàm public drawRoute trong MapActivity
                        ((MapActivity) getActivity()).drawRoute(
                                new GeoPoint(currentLat, currentLng),
                                new GeoPoint(storeLat, storeLng)
                        );
                    }
                    dismiss(); // Đóng BottomSheet sau khi nhấn
                } else {
                    // Nếu vẫn chưa có vị trí hiện tại
                    Toast.makeText(getContext(), "Không tìm thấy vị trí hiện tại, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                }
            });
            // ⭐ KẾT THÚC SỬA LOGIC NÚT CHỈ ĐƯỜNG ⭐

            btnCallStore.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy ứng dụng gọi điện", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}