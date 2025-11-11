package com.example.clothingshopapp.ui.cart;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clothingshopapp.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapAddressSelectionActivity extends AppCompatActivity {

    private static final String TAG = "MapAddressSelection";
    private MapView mapView;
    private TextView selectedAddressText;
    private Button confirmAddressButton;
    private EditText searchAddressInput;
    private ImageView searchAddressButton;

    private String selectedAddressString = null;
    private GeoPoint currentCenter;
    private boolean isMapMoving = false;

    // ⭐ Biến kiểm tra xem địa chỉ có hợp lệ (trong VN) không
    private boolean isValidAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_address_selection);

        initViews();
        setupMap();
        setupListeners();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        selectedAddressText = findViewById(R.id.selectedAddressText);
        confirmAddressButton = findViewById(R.id.confirmAddressButton);
        searchAddressInput = findViewById(R.id.searchAddressInput);
        searchAddressButton = findViewById(R.id.searchAddressButton);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(16.0);
        // Ưu tiên TP.HCM
        GeoPoint startPoint = new GeoPoint(10.7769, 106.7009);
        mapController.setCenter(startPoint);
        getAddressFromGeoPoint(startPoint); // "Dịch" vị trí ban đầu

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                isMapMoving = true;
                selectedAddressText.setText("Đang di chuyển...");
                isValidAddress = false; // Vô hiệu hóa địa chỉ khi đang di chuyển
                return false;
            }
            @Override
            public boolean onZoom(ZoomEvent event) {
                isMapMoving = true;
                selectedAddressText.setText("Đang di chuyển...");
                isValidAddress = false;
                return false;
            }
        });

        // "Bắt" sự kiện khi map ngừng di chuyển
        mapView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (isMapMoving && !mapView.isAnimating()) {
                isMapMoving = false;
                currentCenter = (GeoPoint) mapView.getMapCenter();
                Log.d(TAG, "Map Idle at: " + currentCenter.getLatitude() + ", " + currentCenter.getLongitude());
                getAddressFromGeoPoint(currentCenter); // "Dịch" vị trí mới
            }
        });
    }

    private void setupListeners() {
        confirmAddressButton.setOnClickListener(v -> {
            // ⭐ SỬA: Chỉ cho phép xác nhận khi địa chỉ hợp lệ
            if (isValidAddress && selectedAddressString != null && currentCenter != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SELECTED_ADDRESS", selectedAddressString);
                resultIntent.putExtra("LATITUDE", currentCenter.getLatitude());
                resultIntent.putExtra("LONGITUDE", currentCenter.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Báo lỗi nếu địa chỉ không hợp lệ
                Toast.makeText(this, "Vui lòng chọn một địa chỉ hợp lệ trong Việt Nam", Toast.LENGTH_SHORT).show();
            }
        });

        searchAddressButton.setOnClickListener(v -> handleSearch());
        searchAddressInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handleSearch();
                return true;
            }
            return false;
        });
    }

    private void handleSearch() {
        String addressString = searchAddressInput.getText().toString();
        if (addressString.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchAddressInput.getWindowToken(), 0);
        Toast.makeText(this, "Đang tìm kiếm: " + addressString, Toast.LENGTH_SHORT).show();
        // Chạy "thằng làm thuê" (Tìm kiếm)
        new ForwardGeocodingTask().execute(addressString);
    }

    private void getAddressFromGeoPoint(GeoPoint geoPoint) {
        if (geoPoint == null) return;
        selectedAddressText.setText("Đang tìm địa chỉ...");
        selectedAddressString = null;
        isValidAddress = false; // Reset
        // Chạy "thằng làm thuê" (Dịch ngược)
        new ReverseGeocodingTask().execute(geoPoint);
    }

    // ⭐⭐⭐ SỬA HÀM "DỊCH NGƯỢC" (Khi di chuyển map) ⭐⭐⭐
    private class ReverseGeocodingTask extends AsyncTask<GeoPoint, Void, Address> {
        @Override
        protected Address doInBackground(GeoPoint... params) {
            GeoPoint point = params[0];
            // Dùng GeocoderNominatim của osmdroid
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getUserAgentValue());
            try {
                List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses.get(0); // Trả về Address
                } else {
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Reverse Geocoding error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address == null) {
                selectedAddressString = null;
                isValidAddress = false;
                selectedAddressText.setText("Không tìm thấy địa chỉ tại vị trí này");
                return;
            }

            // KIỂM TRA QUỐC GIA
            String countryCode = address.getCountryCode();
            Log.d(TAG, "Reverse Geocode Country: " + countryCode);

            if (countryCode != null && countryCode.equalsIgnoreCase("vn")) {
                // HỢP LỆ (Trong Việt Nam)
                StringBuilder addressBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressBuilder.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) addressBuilder.append(", ");
                }
                selectedAddressString = addressBuilder.toString();
                selectedAddressText.setText(selectedAddressString);
                isValidAddress = true; // Đánh dấu hợp lệ
            } else {
                // KHÔNG HỢP LỆ (Ngoài Việt Nam)
                selectedAddressString = null;
                isValidAddress = false;
                selectedAddressText.setText("Địa chỉ ngoài Việt Nam. Vui lòng chọn lại.");
            }
        }
    }

    // ⭐⭐⭐ SỬA HÀM "DỊCH XUÔI" (Khi tìm kiếm) ⭐⭐⭐
    private class ForwardGeocodingTask extends AsyncTask<String, Void, Address> {
        @Override
        protected Address doInBackground(String... params) {
            String addressString = params[0];
            GeocoderNominatim geocoder = new GeocoderNominatim(Configuration.getInstance().getUserAgentValue());
            try {
                // Vẫn ưu tiên tìm ở VN
                List<Address> addresses = geocoder.getFromLocationName(addressString + ", Ho Chi Minh City, Vietnam", 1);
                if (addresses != null && !addresses.isEmpty()) {
                    return addresses.get(0); // Trả về Address
                } else {
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "Forward Geocoding error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address == null) {
                Toast.makeText(MapAddressSelectionActivity.this, "Không tìm thấy địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            // KIỂM TRA QUỐC GIA
            String countryCode = address.getCountryCode();
            Log.d(TAG, "Forward Geocode Country: " + countryCode);

            if (countryCode != null && countryCode.equalsIgnoreCase("vn")) {
                // HỢP LỆ (Trong Việt Nam)
                GeoPoint resultPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                mapView.getController().animateTo(resultPoint);
                // getAddressFromGeoPoint(resultPoint) sẽ tự động chạy khi map ngừng di chuyển
            } else {
                // KHÔNG HỢP LỆ (Ngoài Việt Nam)
                Toast.makeText(MapAddressSelectionActivity.this, "Chỉ tìm kiếm địa chỉ trong Việt Nam", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- Lifecycle ---
    @Override
    public void onResume(){ super.onResume(); mapView.onResume(); }
    @Override
    public void onPause(){ super.onPause(); mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDetach(); }
}