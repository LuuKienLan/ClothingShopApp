package com.example.clothingshopapp.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.clothingshopapp.R;
import com.google.android.material.button.MaterialButton;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageView backButton, menuButton;
    private LinearLayout ongoingTab, historyTab;
    private TextView ongoingText, historyText;
    private View ongoingIndicator, historyIndicator;
    private androidx.core.widget.NestedScrollView ongoingContainer, historyContainer;

    private MaterialButton trackOrder1, trackOrder2, trackOrder3;
    private MaterialButton cancelOrder1, cancelOrder2, cancelOrder3;

    private boolean isOngoingSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Header
        backButton = findViewById(R.id.backButton);
        menuButton = findViewById(R.id.menuButton);

        // Tabs
        ongoingTab = findViewById(R.id.ongoingTab);
        historyTab = findViewById(R.id.historyTab);
        ongoingText = findViewById(R.id.ongoingText);
        historyText = findViewById(R.id.historyText);
        ongoingIndicator = findViewById(R.id.ongoingIndicator);
        historyIndicator = findViewById(R.id.historyIndicator);

        // Containers
        ongoingContainer = findViewById(R.id.ongoingContainer);
        historyContainer = findViewById(R.id.historyContainer);

        // Order buttons
        trackOrder1 = findViewById(R.id.trackOrder1);
        trackOrder2 = findViewById(R.id.trackOrder2);
        trackOrder3 = findViewById(R.id.trackOrder3);

        cancelOrder1 = findViewById(R.id.cancelOrder1);
        cancelOrder2 = findViewById(R.id.cancelOrder2);
        cancelOrder3 = findViewById(R.id.cancelOrder3);
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Menu button
        menuButton.setOnClickListener(v ->
                Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show()
        );

        // Tabs
        ongoingTab.setOnClickListener(v -> selectTab(true));
        historyTab.setOnClickListener(v -> selectTab(false));

        // Track Order buttons
        trackOrder1.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #162432", Toast.LENGTH_SHORT).show()
        );
        trackOrder2.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #242432", Toast.LENGTH_SHORT).show()
        );
        trackOrder3.setOnClickListener(v ->
                Toast.makeText(this, "Tracking order #240112", Toast.LENGTH_SHORT).show()
        );

        // Cancel Order buttons
        cancelOrder1.setOnClickListener(v -> showCancelDialog("T-shirt", "#162432"));
        cancelOrder2.setOnClickListener(v -> showCancelDialog("Jacket - Nike", "#242432"));
        cancelOrder3.setOnClickListener(v -> showCancelDialog("Pant", "#240112"));
    }

    private void selectTab(boolean isOngoing) {
        isOngoingSelected = isOngoing;

        if (isOngoing) {
            // Ongoing selected
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            ongoingText.setTypeface(null, android.graphics.Typeface.BOLD);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));

            // History unselected
            historyText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            historyText.setTypeface(null, android.graphics.Typeface.NORMAL);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            Toast.makeText(this, "Showing ongoing orders", Toast.LENGTH_SHORT).show();
        } else {
            // History selected
            historyText.setTextColor(ContextCompat.getColor(this, R.color.coral));
            historyText.setTypeface(null, android.graphics.Typeface.BOLD);
            historyIndicator.setBackgroundColor(ContextCompat.getColor(this, R.color.coral));

            // Ongoing unselected
            ongoingText.setTextColor(ContextCompat.getColor(this, R.color.gray_text));
            ongoingText.setTypeface(null, android.graphics.Typeface.NORMAL);
            ongoingIndicator.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            Toast.makeText(this, "Showing order history", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCancelDialog(String productName, String orderNumber) {
        // TODO: Show cancel confirmation dialog
        Toast.makeText(this,
                "Cancel order " + orderNumber + " - " + productName + "?",
                Toast.LENGTH_SHORT).show();
    }
}