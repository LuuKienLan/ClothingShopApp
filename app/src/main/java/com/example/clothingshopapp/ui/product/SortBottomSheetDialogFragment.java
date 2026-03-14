package com.example.clothingshopapp.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.clothingshopapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private RadioGroup sortRadioGroup;
    private SortListener sortListener;
    private int currentSortOptionId;

    public interface SortListener {
        void onSortApplied(int sortId, String title);
    }

    public static SortBottomSheetDialogFragment newInstance(int currentSortId) {
        SortBottomSheetDialogFragment fragment = new SortBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putInt("currentSortId", currentSortId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSortOptionId = getArguments().getInt("currentSortId", R.id.sort_default);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sort, container, false);

        sortRadioGroup = view.findViewById(R.id.sortRadioGroup);

        if (sortRadioGroup.findViewById(currentSortOptionId) != null) {
            sortRadioGroup.check(currentSortOptionId);
        } else {
            sortRadioGroup.check(R.id.sort_default);
        }

        sortRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null) {
                String title = checkedRadioButton.getText().toString();
                if (sortListener != null) {
                    sortListener.onSortApplied(checkedId, title);
                }
            }
            dismiss();
        });

        return view;
    }

    public void setSortListener(SortListener listener) {
        this.sortListener = listener;
    }
}