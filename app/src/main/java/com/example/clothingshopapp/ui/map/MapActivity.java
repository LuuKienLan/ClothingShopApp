package com.example.clothingshopapp.ui.map;

import android.Manifest;
import android.annotation.SuppressLint; // ⭐ IMPORT MỚI
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.adapter.StoreListAdapter;

// ⭐ IMPORT CÁC THƯ VIỆN GPS "XỊN"
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements StoreListAdapter.OnStoreClickListener {

    private static final String TAG = "MapActivity";

    private MapView mapView = null;
    private MaterialToolbar toolbar;
    private MyLocationNewOverlay myLocationOverlay;
    private GeoPoint currentLocation = null;
    private FloatingActionButton fabMyLocation;
    private RecyclerView storeListRecyclerView;
    private StoreListAdapter storeListAdapter;
    private List<StoreLocation> storeList = new ArrayList<>();
    private ItemizedIconOverlay<StoreOverlayItem> markerOverlay;
    private Polyline currentRoadOverlay;
    private RoadManager roadManager;

    // ⭐ THÊM BIẾN "SÚNG BẮN" GPS
    private FusedLocationProviderClient fusedLocationClient;

    // (Inner class StoreLocation... giữ nguyên)
    public static class StoreLocation {
        public String id; public String name; public String address; public String hours; public String phone;
        public double latitude; public double longitude;
        public StoreLocation(String id, String name, String address, String hours, String phone, double latitude, double longitude) {
            this.id = id; this.name = name; this.address = address; this.hours = hours;
            this.phone = phone; this.latitude = latitude; this.longitude = longitude;
        }
        public GeoPoint getGeoPoint() { return new GeoPoint(latitude, longitude); }
    }
    public static class StoreOverlayItem extends OverlayItem {
        private final StoreLocation storeLocation;
        public StoreOverlayItem(String aTitle, String aSnippet, GeoPoint aGeoPoint, StoreLocation store) {
            super(aTitle, aSnippet, aGeoPoint);
            this.storeLocation = store;
        }
        public StoreLocation getStoreLocation() { return storeLocation; }
    }

    // (Thông tin cửa hàng... giữ nguyên)
    private static final String FPTU_ID = "FPTU";
    private static final String FPTU_NAME = "Clothing Shop - Chi nhánh FPTU";
    private static final String FPTU_ADDRESS = "Lô E2a-7, Đường D1, Long Thạnh Mỹ, Quận 9, TP.HCM";
    private static final String FPTU_HOURS = "Giờ mở cửa: 8:00 - 21:00 hàng ngày";
    private static final String FPTU_PHONE = "+84 987 654 321";
    private static final double FPTU_LATITUDE = 10.8411;
    private static final double FPTU_LONGITUDE = 106.8099;
    private static final String Q1_ID = "Q1";
    private static final String Q1_NAME = "Clothing Shop - Chi nhánh Quận 1";
    private static final String Q1_ADDRESS = "123 Nguyễn Huệ, P. Bến Nghé, Q.1, TP.HCM";
    private static final String Q1_HOURS = "Giờ mở cửa: 9:00 - 22:00 hàng ngày";
    private static final String Q1_PHONE = "+84 123 456 789";
    private static final double Q1_LATITUDE = 10.7758;
    private static final double Q1_LONGITUDE = 106.7019;

    // (Permission Launcher giữ nguyên)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setupMyLocationOverlay(true);
                } else {
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_LONG).show();
                    if (!storeList.isEmpty()) {
                        centerMapOnStore(storeList.get(0));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        initViews();
        roadManager = new OSRMRoadManager(this, getPackageName());

        // ⭐ NẠP "ĐẠN" CHO "SÚNG" GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createStoreList();
        setupMap();
        addStoreMarkers();
        setupStoreListRecyclerView();
        requestLocationPermission();

        toolbar.setNavigationOnClickListener(v -> finish());
        fabMyLocation.setOnClickListener(v -> goToMyLocation()); // ⭐ Nút này sẽ gọi hàm "xịn"
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.mapViewDisplay);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        storeListRecyclerView = findViewById(R.id.storeListRecyclerView);
    }

    // (Các hàm từ createStoreList đến setupStoreListRecyclerView giữ nguyên)

    private void createStoreList() {
        storeList.clear();
        storeList.add(new StoreLocation(FPTU_ID, FPTU_NAME, FPTU_ADDRESS, FPTU_HOURS, FPTU_PHONE, FPTU_LATITUDE, FPTU_LONGITUDE));
        storeList.add(new StoreLocation(Q1_ID, Q1_NAME, Q1_ADDRESS, Q1_HOURS, Q1_PHONE, Q1_LATITUDE, Q1_LONGITUDE));
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(14.0);
        if (!storeList.isEmpty()) {
            mapController.setCenter(storeList.get(0).getGeoPoint());
        }
    }

    private void centerMapOnStore(StoreLocation store){
        if (mapView != null && store != null) {
            mapView.getController().setZoom(17.5);
            mapView.getController().animateTo(store.getGeoPoint());
        }
    }

    private void addStoreMarkers() {
        if (mapView == null || storeList.isEmpty()) return;
        ArrayList<StoreOverlayItem> items = new ArrayList<>();
        Drawable markerIcon = ContextCompat.getDrawable(this, R.drawable.ic_map_pin_red);
        for (StoreLocation store : storeList) {
            StoreOverlayItem item = new StoreOverlayItem(store.name, store.address, store.getGeoPoint(), store);
            item.setMarker(markerIcon);
            items.add(item);
        }
        markerOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<StoreOverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final StoreOverlayItem item) {
                        StoreLocation clickedStore = item.getStoreLocation();
                        if (clickedStore != null) {
                            showStoreBottomSheet(clickedStore);
                            centerMapOnStore(clickedStore);
                        }
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final StoreOverlayItem item) {
                        return false;
                    }
                }, getApplicationContext());
        mapView.getOverlays().clear();
        mapView.getOverlays().add(markerOverlay);
        mapView.invalidate();
    }


    private void setupStoreListRecyclerView() {
        storeListAdapter = new StoreListAdapter(storeList, this);
        storeListRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        storeListRecyclerView.setAdapter(storeListAdapter);
    }

    @Override
    public void onStoreClick(StoreLocation store) {
        centerMapOnStore(store);
        showStoreBottomSheet(store);
    }

    private void showStoreBottomSheet(StoreLocation store) {
        StoreInfoBottomSheet bottomSheet = StoreInfoBottomSheet.newInstance(
                store.name, store.address, store.hours, store.phone,
                store.latitude, store.longitude,
                currentLocation != null ? currentLocation.getLatitude() : null,
                currentLocation != null ? currentLocation.getLongitude() : null
        );
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay(false);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void setupMyLocationOverlay(boolean zoomToFirstFix) {
        if (mapView == null) return;
        if (myLocationOverlay != null) {
            mapView.getOverlays().remove(myLocationOverlay);
        }
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(() -> {
            Log.d(TAG, "First location fix received.");
            runOnUiThread(() -> {
                // Chúng ta vẫn dùng VỊ TRÍ CŨ (CACHE) để hiện cái chấm xanh
                // (Vì hàm này chỉ chạy 1 lần)
                // Nhưng chúng ta sẽ update nó bằng nút FAB
                currentLocation = myLocationOverlay.getMyLocation();
                if (currentLocation != null && mapView != null) {
                    Log.d(TAG, "Current location (cached): " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    if (zoomToFirstFix) {
                        IMapController mapController = mapView.getController();
                        mapController.setZoom(17.0);
                        mapController.animateTo(currentLocation);
                    }
                } else {
                    Log.w(TAG, "Could not get current location on first fix.");
                    if(zoomToFirstFix && !storeList.isEmpty()) centerMapOnStore(storeList.get(0));
                }
            });
        });
        mapView.getOverlays().add(myLocationOverlay);
        mapView.invalidate();
    }

    // ⭐⭐⭐ BẮT ĐẦU SỬA (HÀM "XỊN") ⭐⭐⭐
    /**
     * Hàm này đã được viết lại để dùng getCurrentLocation
     * (Giống hệt CheckoutActivity)
     */
    @SuppressLint("MissingPermission")
    private void goToMyLocation() {
        // 1. Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Vui lòng cấp quyền vị trí", Toast.LENGTH_SHORT).show();
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        // 2. Bắt đầu "bắn" GPS
        Toast.makeText(this, "Đang lấy vị trí hiện tại ...", Toast.LENGTH_SHORT).show();
        fabMyLocation.setEnabled(false); // Khóa nút

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    fabMyLocation.setEnabled(true); // Mở nút
                    if (location != null && mapView != null) {
                        // 3. "Bắn" trúng -> Lấy tọa độ MỚI
                        GeoPoint newCurrentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        this.currentLocation = newCurrentLocation; // Cập nhật vị trí "xịn"

                        Log.d(TAG, "goToMyLocation thành công: " + newCurrentLocation.toDoubleString());

                        // 4. Zoom
                        mapView.getController().animateTo(newCurrentLocation);
                        mapView.getController().setZoom(17.5);
                    } else {
                        // "Bắn" xịt (null)
                        Toast.makeText(this, "Không tìm thấy vị trí. Vui lòng bật GPS!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    // "Súng" hỏng
                    fabMyLocation.setEnabled(true);
                    Log.e(TAG, "goToMyLocation (Hàm 'xịn') thất bại", e);
                    Toast.makeText(this, "Lỗi khi lấy vị trí GPS.", Toast.LENGTH_SHORT).show();
                });
    }
    // ⭐⭐⭐ KẾT THÚC SỬA ⭐⭐⭐

    // (Các hàm còn lại: drawRoute, UpdateRoadTask, Lifecycle... giữ nguyên)

    public void drawRoute(GeoPoint start, GeoPoint end) {
        if (mapView == null) return;
        if (currentRoadOverlay != null) {
            mapView.getOverlays().remove(currentRoadOverlay);
        }
        new UpdateRoadTask().execute(start, end);
    }

    private class UpdateRoadTask extends AsyncTask<GeoPoint, Void, Road> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MapActivity.this, "Đang tìm đường đi...", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected Road doInBackground(GeoPoint... params) {
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(params[0]);
            waypoints.add(params[1]);
            try {
                return roadManager.getRoad(waypoints);
            } catch (Exception e) {
                Log.e(TAG, "Error getting road", e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(Road road) {
            super.onPostExecute(road);
            if (road == null) {
                Toast.makeText(MapActivity.this, "Lỗi: Không thể tìm thấy đường đi", Toast.LENGTH_SHORT).show();
                return;
            }
            if (road.mStatus != Road.STATUS_OK) {
                Toast.makeText(MapActivity.this, "Lỗi: " + road.mStatus, Toast.LENGTH_SHORT).show();
                return;
            }
            currentRoadOverlay = RoadManager.buildRoadOverlay(road);
            currentRoadOverlay.setColor(Color.BLUE);
            currentRoadOverlay.setWidth(10);
            mapView.getOverlays().add(currentRoadOverlay);
            if (road.mBoundingBox != null) {
                mapView.post(() -> {
                    mapView.zoomToBoundingBox(road.mBoundingBox, true, 100);
                });
            }
            mapView.invalidate();
            Toast.makeText(MapActivity.this, "Đã tìm thấy đường đi!", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Lifecycle methods (onResume, onPause, onDestroy) ---
    @Override
    public void onResume(){
        super.onResume();
        if (mapView != null) mapView.onResume();
        if (myLocationOverlay != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay.enableMyLocation();
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        if (mapView != null) mapView.onPause();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay = null;
        }
    }
}