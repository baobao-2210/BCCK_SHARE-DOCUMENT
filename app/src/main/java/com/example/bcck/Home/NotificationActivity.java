package com.example.bcck.Home;

// File: NotificationActivity.java
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bcck.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;
    private TextView tvUnreadCount;
    private TextView btnMarkAllRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        tvUnreadCount = findViewById(R.id.tv_unread_count);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        recyclerView = findViewById(R.id.recyclerViewNotifications);

        // 1. Dữ liệu giả lập
        addDummyData();

        // 2. Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        adapter.setOnNotificationClickListener(this); // Thiết lập Listener
        recyclerView.setAdapter(adapter);

        updateUnreadCountText();
        setupEventListeners();
    }

    // **Xử lý sự kiện Click từ Adapter**
    @Override
    public void onNotificationClick(int position) {
        NotificationItem item = notificationList.get(position);

        // 1. Xử lý logic: Đánh dấu thông báo là đã đọc
        if (!item.isRead()) {
            item.setRead(true);
            // 2. Cập nhật giao diện của item đó
            adapter.notifyItemChanged(position);
            // 3. Cập nhật số lượng thông báo chưa đọc trên header
            updateUnreadCountText();
            Toast.makeText(this, "Thông báo: " + item.getTitle() + " đã được đọc.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mở chi tiết thông báo...", Toast.LENGTH_SHORT).show();
        }

        // TODO: Chuyển sang màn hình chi tiết tương ứng (Intent to Detail Activity)
    }

    // Xử lý sự kiện cho các nút
    private void setupEventListeners() {
        // Nút Đánh dấu đã đọc tất cả
        btnMarkAllRead.setOnClickListener(v -> {
            markAllAsRead();
        });

        // Nút Quay lại
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    // Thao tác đánh dấu tất cả là đã đọc
    private void markAllAsRead() {
        int itemsChanged = 0;
        for (int i = 0; i < notificationList.size(); i++) {
            NotificationItem item = notificationList.get(i);
            if (!item.isRead()) {
                item.setRead(true);
                adapter.notifyItemChanged(i); // Cập nhật từng item
                itemsChanged++;
            }
        }
        if (itemsChanged > 0) {
            updateUnreadCountText();
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc.", Toast.LENGTH_SHORT).show();
        }
    }

    // Cập nhật text số lượng thông báo chưa đọc
    private void updateUnreadCountText() {
        long unreadCount = notificationList.stream().filter(item -> !item.isRead()).count();
        tvUnreadCount.setText("Bạn có " + unreadCount + " thông báo chưa đọc");

        // Tùy chỉnh màu hoặc ẩn nút "Đánh dấu đã đọc" nếu count = 0
        if (unreadCount == 0) {
            btnMarkAllRead.setVisibility(View.GONE);
        } else {
            btnMarkAllRead.setVisibility(View.VISIBLE);
        }
    }

    private void addDummyData() {
        // Sử dụng R.drawable.ic_document, R.drawable.ic_comment, R.drawable.ic_like, R.drawable.ic_group
        // (Bạn cần tạo các vector assets này)
        notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem("Tài liệu mới", "TS. Nguyễn Văn A đã đăng tài liệu mới cho môn Lập trình hướng đối tượng", "5 phút trước", R.drawable.ic_gdrive, false));
        notificationList.add(new NotificationItem("Bình luận mới", "Trần Văn B đã bình luận về tài liệu \"CTDL - Chương 3\" của bạn", "30 phút trước", R.drawable.ic_dropbox, false));
        notificationList.add(new NotificationItem("Lượt thích mới", "Lê Thị C và 5 người khác đã thích tài liệu của bạn", "1 giờ trước", R.drawable.ic_like, false));
        notificationList.add(new NotificationItem("Nhóm học tập", "Bạn được thêm vào nhóm \"AI & Machine Learning\"", "2 giờ trước", R.drawable.ic_message, true));
        notificationList.add(new NotificationItem("Tài liệu mới", "Thầy X đã đăng tài liệu mới cho môn Toán rời rạc", "Hôm qua", R.drawable.ic_gdrive, true));
    }
}
