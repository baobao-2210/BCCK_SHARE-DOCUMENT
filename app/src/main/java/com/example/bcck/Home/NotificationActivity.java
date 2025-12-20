package com.example.bcck.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private final ArrayList<NotificationItem> list = new ArrayList<>();

    private FirebaseFirestore db;
    private String myUid;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // ✅ 1) Back của ActionBar (nếu có)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ✅ 2) Back vật lý của máy
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // ✅ 3) Back icon trong layout (NẾU BẠN CÓ)
        // Nếu ID khác, đổi R.id.btnBack thành ID icon back của bạn
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(list, item -> {
            markRead(item);

            if (item.getChatId() != null && !item.getChatId().trim().isEmpty()) {
                Intent i = new Intent(this, ChatDetailActivity.class);
                i.putExtra("chatId", item.getChatId());
                startActivity(i);
            }
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (myUid == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listenNotifications();
    }

    // ✅ Khi bấm nút back trên ActionBar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void listenNotifications() {
        if (listener != null) listener.remove();

        listener = db.collection("users").document(myUid)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Lỗi load thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    list.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String id = d.getId();
                        String chatId = d.getString("chatId");
                        String title = d.getString("title");
                        String content = d.getString("content");
                        Boolean isReadB = d.getBoolean("isRead");
                        Timestamp ts = d.getTimestamp("createdAt");

                        boolean isRead = isReadB != null && isReadB;
                        String time = formatTime(ts);

                        int iconRes = android.R.drawable.ic_dialog_email;

                        list.add(new NotificationItem(
                                id,
                                chatId != null ? chatId : "",
                                title != null ? title : "Thông báo",
                                content != null ? content : "",
                                time,
                                iconRes,
                                isRead
                        ));
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private String formatTime(Timestamp ts) {
        if (ts == null) return "";
        Date date = ts.toDate();
        return new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date);
    }

    private void markRead(NotificationItem item) {
        if (item.isRead()) return;

        db.collection("users").document(myUid)
                .collection("notifications")
                .document(item.getId())
                .update("isRead", true);

        item.setRead(true);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
