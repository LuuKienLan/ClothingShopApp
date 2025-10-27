package com.example.clothingshopapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clothingshopapp.R;
import com.example.clothingshopapp.ui.map.MapActivity.StoreLocation; // Import inner class
import java.util.List;

public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.StoreViewHolder> {

    private List<StoreLocation> storeList;
    private OnStoreClickListener listener;

    public interface OnStoreClickListener {
        void onStoreClick(StoreLocation store);
    }

    public StoreListAdapter(List<StoreLocation> storeList, OnStoreClickListener listener) {
        this.storeList = storeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store_location, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreLocation store = storeList.get(position);
        holder.bind(store, listener);
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    // Method to update the list if loaded asynchronously
    public void updateData(List<StoreLocation> newStoreList) {
        this.storeList.clear();
        this.storeList.addAll(newStoreList);
        notifyDataSetChanged();
    }


    static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView storeName, storeAddress;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeName = itemView.findViewById(R.id.storeItemName);
            storeAddress = itemView.findViewById(R.id.storeItemAddress);
        }

        public void bind(final StoreLocation store, final OnStoreClickListener listener) {
            storeName.setText(store.name);
            storeAddress.setText(store.address);
            itemView.setOnClickListener(v -> listener.onStoreClick(store));
        }
    }
}