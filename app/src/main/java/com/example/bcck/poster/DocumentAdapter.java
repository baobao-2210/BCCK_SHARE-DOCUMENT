package com.example.bcck.poster;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documentList;
    private OnDocumentClickListener clickListener;

    public interface OnDocumentClickListener {
        void onDocumentClick(Document document);
        void onMoreClick(Document document);
    }
    public DocumentAdapter(List<Document> documentList, OnDocumentClickListener listener) {
        this.documentList = documentList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document doc = documentList.get(position);

        holder.tvAuthorName.setText(doc.getAuthorName());
        holder.tvDocTitle.setText(doc.getTitle());
        holder.tvSubject1.setText(doc.getSubject());
        holder.tvTeacher.setText(doc.getTeacher());
        holder.tvMajor.setText(doc.getMajor());
        holder.tvDownloads.setText(String.valueOf(doc.getDownloads()));
        holder.tvLikes.setText(String.valueOf(doc.getLikes()));
        holder.tvRating.setText(String.valueOf(doc.getRating()));

        // Set document type badge
        holder.docTypeBadge.setText(doc.getDocType());
        if ("PDF".equals(doc.getDocType())) {
            holder.docTypeBadge.setTextColor(Color.parseColor("#FF5722"));
            holder.docTypeBadge.setBackgroundColor(Color.parseColor("#FFE5E0"));
        } else if ("PPT".equals(doc.getDocType())) {
            holder.docTypeBadge.setTextColor(Color.parseColor("#FF9800"));
            holder.docTypeBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onDocumentClick(doc);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMoreClick(doc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName, tvDocTitle, docTypeBadge, tvSubject1, tvTeacher, tvMajor;
        TextView tvDownloads, tvLikes, tvRating;
        ImageView btnMore;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvDocTitle = itemView.findViewById(R.id.tvDocTitle);
            docTypeBadge = itemView.findViewById(R.id.docTypeBadge);
            tvSubject1 = itemView.findViewById(R.id.tvSubject1);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvMajor = itemView.findViewById(R.id.tvMajor);
            tvDownloads = itemView.findViewById(R.id.tvDownloads);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}