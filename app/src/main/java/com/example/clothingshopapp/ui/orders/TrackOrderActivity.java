package com.example.clothingshopapp.ui.orders;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.data.model.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackOrderActivity extends AppCompatActivity {

    private static final String TAG = "TrackOrderActivity";
    private MapView mapView;
    private ProgressBar progressBar;
    private TextView etaText, orderStatusText; // TextViews cho thông tin
    private RoadManager roadManager;
    private Polyline roadOverlay; // Đường đầy đủ (màu xám)
    private Polyline roadTraveledOverlay; // Đường đã đi (màu xanh)
    private Marker shipperMarker; // Marker cho shipper
    private Order currentOrder;
    private DatabaseReference orderRef; // Tham chiếu đến đơn hàng trên Firebase

    // Vị trí cửa hàng (FPTU)
    private final GeoPoint storeLocation = new GeoPoint(10.8411, 106.8099);

    // Biến cho mô phỏng
    private Handler simulationHandler = new Handler(Looper.getMainLooper());
    private List<GeoPoint> routePoints;
    private int currentPointIndex = 0;
    private double totalDuration = 0; // Tổng thời gian (giây)
    private Runnable simulationRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cấu hình osmdroid
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_track_order);

        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gán tham chiếu orderRef
        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        roadManager = new OSRMRoadManager(this, getPackageName());

        initViews();
        setupMap();
        loadOrderAndDrawRoute(); // Không cần truyền orderId nữa
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        mapView = findViewById(R.id.mapViewTracking);
        progressBar = findViewById(R.id.trackingProgressBar);
        etaText = findViewById(R.id.etaText);
        orderStatusText = findViewById(R.id.orderStatusText);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(14.0);
        mapController.setCenter(storeLocation);
    }

    private void loadOrderAndDrawRoute() {
        progressBar.setVisibility(View.VISIBLE);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentOrder = snapshot.getValue(Order.class);
                if (currentOrder != null && currentOrder.getLatitude() != 0 && currentOrder.getLongitude() != 0) {
                    GeoPoint customerLocation = new GeoPoint(currentOrder.getLatitude(), currentOrder.getLongitude());
                    updateStatusUI(currentOrder.getStatus()); // Cập nhật trạng thái ban đầu
                    new UpdateRoadTask().execute(storeLocation, customerLocation);
                } else {
                    Toast.makeText(TrackOrderActivity.this, "Lỗi: Không tìm thấy địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TrackOrderActivity.this, "Lỗi tải đơn hàng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMarkers(GeoPoint start, GeoPoint end) {
        // Marker cửa hàng
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(start);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_store_location));
        startMarker.setTitle("Cửa hàng");
        mapView.getOverlays().add(startMarker);

        // Marker người nhận (nhà)
        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(end);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_map_pin_red));
        endMarker.setTitle("Địa chỉ của bạn");
        mapView.getOverlays().add(endMarker);

        // Marker cho Shipper
        shipperMarker = new Marker(mapView);
        shipperMarker.setPosition(start); // Bắt đầu từ cửa hàng
        shipperMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER); // Tâm icon
        shipperMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_shipper_bike));
        shipperMarker.setTitle("Shipper");
        mapView.getOverlays().add(shipperMarker);
    }

    // AsyncTask để tìm đường đi
    private class UpdateRoadTask extends AsyncTask<GeoPoint, Void, Road> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Road doInBackground(GeoPoint... params) {
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(params[0]); // Điểm bắt đầu (cửa hàng)
            waypoints.add(params[1]); // Điểm kết thúc (nhà)
            try {
                return roadManager.getRoad(waypoints);
            } catch (Exception e) {
                Log.e(TAG, "Error getting road", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Road road) {
            progressBar.setVisibility(View.GONE);
            if (road == null || road.mStatus != Road.STATUS_OK) {
                Toast.makeText(TrackOrderActivity.this, "Lỗi: Không thể tìm thấy đường đi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roadOverlay != null) mapView.getOverlays().remove(roadOverlay);
            if (roadTraveledOverlay != null) mapView.getOverlays().remove(roadTraveledOverlay);

            // 1. Vẽ toàn bộ đường đi (màu xám nhạt)
            roadOverlay = RoadManager.buildRoadOverlay(road);
            roadOverlay.setColor(Color.LTGRAY);
            roadOverlay.setWidth(10);
            mapView.getOverlays().add(roadOverlay);

            // 2. Vẽ đường đã đi (màu xanh)
            roadTraveledOverlay = new Polyline(mapView);
            roadTraveledOverlay.setColor(Color.BLUE);
            roadTraveledOverlay.setWidth(12);
            mapView.getOverlays().add(roadTraveledOverlay);

            // 3. Thêm marker
            routePoints = roadOverlay.getPoints();
            addMarkers(routePoints.get(0), routePoints.get(routePoints.size() - 1));

            // 4. Lấy thông tin
            totalDuration = road.mDuration;
            updateEtaText(totalDuration); // Cập nhật ETA ban đầu
            updateStatusUI("Đang giao hàng"); // Cập nhật trạng thái

            // 5. Zoom
            mapView.post(() -> mapView.zoomToBoundingBox(road.mBoundingBox, true, 150));
            mapView.invalidate();

            // 6. Bắt đầu mô phỏng
            currentPointIndex = 0;
            startSimulationLoop();
        }
    }

    // Bắt đầu vòng lặp mô phỏng
    private void startSimulationLoop() {
        simulationRunnable = () -> {
            if (routePoints == null || routePoints.isEmpty()) return; // An toàn

            if (currentPointIndex < routePoints.size()) {
                GeoPoint nextPoint = routePoints.get(currentPointIndex);

                if(shipperMarker != null) shipperMarker.setPosition(nextPoint);

                if(roadTraveledOverlay != null) {
                    List<GeoPoint> traveledPoints = roadTraveledOverlay.getPoints();
                    traveledPoints.add(nextPoint);
                    roadTraveledOverlay.setPoints(traveledPoints);
                }

                double pointsRemaining = routePoints.size() - currentPointIndex;
                double pointsTotal = routePoints.size();
                double durationRemaining = (pointsRemaining / pointsTotal) * totalDuration;
                updateEtaText(durationRemaining);

                mapView.invalidate();
                currentPointIndex++;

                // Tốc độ mô phỏng (ví dụ: 100ms cho nhanh)
                simulationHandler.postDelayed(simulationRunnable, 100);
            } else {
                // Shipper đã đến nơi
                Toast.makeText(TrackOrderActivity.this, "Shipper đã đến nơi!", Toast.LENGTH_LONG).show();
                updateEtaText(0);
                updateStatusUI("Delivered"); // Cập nhật UI

                // ⭐ TỰ ĐỘNG CẬP NHẬT LÊN FIREBASE ⭐
                if (orderRef != null) {
                    orderRef.child("status").setValue("Delivered")
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Order status updated to Delivered"))
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to update order status", e));
                }
            }
        };
        simulationHandler.postDelayed(simulationRunnable, 1000); // Bắt đầu sau 1 giây
    }

    // Cập nhật text Trạng thái
    private void updateStatusUI(String status) {
        if (orderStatusText == null) return;
        orderStatusText.setText(status);

        if (status.equalsIgnoreCase("Delivered") || status.equalsIgnoreCase("Completed")) {
            orderStatusText.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Đang chuẩn bị...")) {
            orderStatusText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
        } else {
            orderStatusText.setTextColor(ContextCompat.getColor(this, R.color.dark_navy));
        }
    }

    // Cập nhật text ETA
    private void updateEtaText(double secondsRemaining) {
        if (etaText == null) return;

        if (secondsRemaining <= 0) {
            etaText.setText("Đã đến nơi");
            return;
        }

        long minutes = (long) (secondsRemaining / 60);
        if (minutes < 1) {
            etaText.setText("Sắp đến nơi...");
        } else {
            etaText.setText(String.format(Locale.US, "Dự kiến sau %d phút", minutes));
        }
    }

    // --- Lifecycle ---
    @Override
    public void onResume(){
        super.onResume();
        if (mapView != null) mapView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simulationRunnable != null) {
            simulationHandler.removeCallbacks(simulationRunnable); // Dừng mô phỏng
        }
        if (mapView != null) mapView.onDetach();
    }
}