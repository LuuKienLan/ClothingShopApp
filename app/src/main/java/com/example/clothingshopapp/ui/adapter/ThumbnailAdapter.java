package com.example.clothingshopapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.clothingshopapp.R;
import java.util.List;

/**
 * Adapter này dùng để "bơm" ảnh thu nhỏ (thumbnails)
 * vào RecyclerView nằm dưới cái ViewPager2.
 */
public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private OnThumbnailClickListener listener;
    private int selectedPosition = 0; // Vị trí đang được chọn (mặc định là 0)

    // Interface để "nói chuyện" ngược lại với Activity
    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public ThumbnailAdapter(Context context, List<String> imageUrls, OnThumbnailClickListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail_image, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.color.gray_icon)
                .into(holder.imageView);

        // Logic "vẽ" viền:
        // Nếu vị trí này là vị trí đang được chọn -> "HIỆN" viền
        if (selectedPosition == position) {
            holder.borderView.setVisibility(View.VISIBLE);
        } else {
            // Nếu không -> "ẨN" viền
            holder.borderView.setVisibility(View.GONE);
        }

        // "Dạy" cho nút cách "nói chuyện"
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThumbnailClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // Hàm "xịn": Dùng để ViewPager2 "ra lệnh" cho Adapter này
    // "Này, người dùng vừa lướt đến ảnh số 2, tô viền cam cho ảnh số 2 đi!"
    public void setSelectedPosition(int position) {
        if (position == selectedPosition || position < 0) return;

        int oldPosition = selectedPosition;
        selectedPosition = position;

        // Báo cho Adapter "vẽ" lại 2 cái: cái cũ (để bỏ viền) và cái mới (để thêm viền)
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    // ViewHolder (chứa 2 view: ảnh và viền)
    static class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        View borderView;

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.thumbnail_image_view);
            borderView = itemView.findViewById(R.id.thumbnail_border);
        }
    }
}