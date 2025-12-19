package com.example.bcck.library;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {

    private List<Collection> collections;
    private OnCollectionListener listener;

    public interface OnCollectionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onItemClick(int position);
    }

    public CollectionAdapter(List<Collection> collections, OnCollectionListener listener) {
        this.collections = collections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);

        holder.tvName.setText(collection.getName());
        holder.tvCount.setText(collection.getDocumentCount() + " tài liệu");

        // Đặt màu cho CardView
        try {
            holder.cardFolder.setCardBackgroundColor(Color.parseColor(collection.getColor()));
        } catch (IllegalArgumentException e) {
            holder.cardFolder.setCardBackgroundColor(Color.parseColor("#5B8DEE"));
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
        holder.imgEdit.setOnClickListener(v -> listener.onEditClick(position));
        holder.imgDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public void updateData(List<Collection> newCollections) {
        this.collections = newCollections;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardFolder;
        TextView tvName, tvCount;
        ImageView imgEdit, imgDelete;

        ViewHolder(View itemView) {
            super(itemView);
            cardFolder = itemView.findViewById(R.id.cardFolder);
            tvName = itemView.findViewById(R.id.tvCollectionName);
            tvCount = itemView.findViewById(R.id.tvDocumentCount);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}
