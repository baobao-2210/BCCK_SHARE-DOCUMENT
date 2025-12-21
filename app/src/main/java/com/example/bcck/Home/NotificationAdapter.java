package com.example.bcck.Home;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnItemClick {
        void onClick(NotificationItem item);
    }

    private final ArrayList<NotificationItem> list;
    private final OnItemClick onItemClick;

    public NotificationAdapter(ArrayList<NotificationItem> list, OnItemClick onItemClick) {
        this.list = list;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationItem item = list.get(position);

        h.tvTitle.setText(item.getTitle());
        h.tvContent.setText(item.getContent());
        h.tvTime.setText(item.getTime());
        h.ivIcon.setImageResource(item.getIconResId());

        // đọc / chưa đọc (in đậm nếu chưa đọc)
        if (item.isRead()) {
            h.tvTitle.setTypeface(null, Typeface.NORMAL);
        } else {
            h.tvTitle.setTypeface(null, Typeface.BOLD);
        }

        // dot chưa đọc
        h.unreadDot.setVisibility(item.isRead() ? View.INVISIBLE : View.VISIBLE);

        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvContent, tvTime;
        View unreadDot;

        public VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            unreadDot = itemView.findViewById(R.id.iv_unread_dot);
        }
    }
}
