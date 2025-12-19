package com.example.bcck.Home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    // Interface để xử lý sự kiện click
    public interface OnNotificationClickListener {
        void onNotificationClick(int position);
    }
    private OnNotificationClickListener clickListener;

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
        holder.tvTime.setText(item.getTime());
        holder.ivIcon.setImageResource(item.getIconResId());

        // **Xử lý trạng thái Đã đọc/Chưa đọc**
        if (item.isRead()) {
            holder.ivUnreadDot.setVisibility(View.GONE);
            holder.notificationItemLayout.setBackgroundResource(R.drawable.notification_read_bg);
        } else {
            holder.ivUnreadDot.setVisibility(View.VISIBLE);
            holder.notificationItemLayout.setBackgroundResource(R.drawable.notification_unread_bg);
            // Màu nền nhẹ hơn nếu chưa đọc
        }

        // **Xử lý sự kiện Click**
        holder.notificationItemLayout.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNotificationClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // ViewHolder
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvContent, tvTime;
        View ivUnreadDot;
        ConstraintLayout notificationItemLayout;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivUnreadDot = itemView.findViewById(R.id.iv_unread_dot);
            notificationItemLayout = itemView.findViewById(R.id.notification_item_layout);

        }
    }
}
