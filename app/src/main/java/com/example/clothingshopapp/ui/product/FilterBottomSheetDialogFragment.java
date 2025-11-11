package com.example.clothingshopapp.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.clothingshopapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private ChipGroup categoryChipGroup, priceChipGroup;
    private Button applyFilterButton;
    private TextView resetFilterButton;
    private FilterListener filterListener;

    private String currentCategory = "Tất cả";
    private int currentPriceRangeId = R.id.chipPriceAll;

    public interface FilterListener {
        void onFilterApplied(String category, int priceRangeId);
    }

    public static FilterBottomSheetDialogFragment newInstance(String category, int priceRangeId) {
        FilterBottomSheetDialogFragment fragment = new FilterBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString("currentCategory", category);
        args.putInt("currentPriceRangeId", priceRangeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentCategory = getArguments().getString("currentCategory", "Tất cả");
            currentPriceRangeId = getArguments().getInt("currentPriceRangeId", R.id.chipPriceAll);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        priceChipGroup = view.findViewById(R.id.priceChipGroup);
        applyFilterButton = view.findViewById(R.id.applyFilterButton);
        resetFilterButton = view.findViewById(R.id.resetFilterButton);

        preselectChips();

        applyFilterButton.setOnClickListener(v -> {
            String selectedCategory = "Tất cả";
            int selectedCategoryId = categoryChipGroup.getCheckedChipId();
            if (selectedCategoryId != View.NO_ID && selectedCategoryId != R.id.chipCategoryAll) {
                selectedCategory = ((Chip) categoryChipGroup.findViewById(selectedCategoryId)).getText().toString();
            }

            int selectedPriceId = priceChipGroup.getCheckedChipId();
            if (selectedPriceId == View.NO_ID) {
                selectedPriceId = R.id.chipPriceAll;
            }

            if (filterListener != null) {
                filterListener.onFilterApplied(selectedCategory, selectedPriceId);
            }
            dismiss();
        });

        resetFilterButton.setOnClickListener(v -> {
            categoryChipGroup.check(R.id.chipCategoryAll);
            priceChipGroup.check(R.id.chipPriceAll);
        });

        return view;
    }

    private void preselectChips() {
        if (priceChipGroup.findViewById(currentPriceRangeId) != null) {
            priceChipGroup.check(currentPriceRangeId);
        } else {
            priceChipGroup.check(R.id.chipPriceAll);
        }

        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(currentCategory)) {
                chip.setChecked(true);
                return;
            }
        }
        categoryChipGroup.check(R.id.chipCategoryAll);
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }
}