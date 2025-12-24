package com.example.bcck.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText etGroupName;
    private EditText etMemberEmail;
    private ImageView btnBack;
    private ImageView btnAddMember;

    private GroupMemberAdapter adapter;
    private final List<String> memberEmails = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etGroupName = findViewById(R.id.etGroupName);
        etMemberEmail = findViewById(R.id.etMemberEmail);
        btnBack = findViewById(R.id.btnBackCreateGroup);
        btnAddMember = findViewById(R.id.btnAddMember);

        RecyclerView rv = findViewById(R.id.recyclerMembers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupMemberAdapter(memberEmails, this::removeMemberEmail);
        rv.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnAddMember.setOnClickListener(v -> addMemberEmail());

        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> createGroup());
    }

    private void addMemberEmail() {
        String email = etMemberEmail.getText() != null ? etMemberEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            Toast.makeText(this, "Nhập gmail thành viên", Toast.LENGTH_SHORT).show();
            return;
        }

        String normalized = email.toLowerCase(Locale.ROOT);
        if (memberEmails.contains(normalized)) {
            Toast.makeText(this, "Email đã được thêm", Toast.LENGTH_SHORT).show();
            return;
        }

        memberEmails.add(normalized);
        etMemberEmail.setText("");
        adapter.notifyDataSetChanged();
    }

    private void removeMemberEmail(String email) {
        if (memberEmails.remove(email)) {
            adapter.notifyDataSetChanged();
        }
    }

    private void createGroup() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String groupName = etGroupName.getText() != null ? etGroupName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(groupName)) {
            Toast.makeText(this, "Nhập tên nhóm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (memberEmails.isEmpty()) {
            Toast.makeText(this, "Thêm ít nhất 1 thành viên", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Set<String> memberUids = new HashSet<>();
        memberUids.add(myUid);

        AtomicInteger pending = new AtomicInteger(memberEmails.size());
        List<String> notFound = new ArrayList<>();

        for (String email : memberEmails) {
            db.collection("users")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snap -> {
                        if (snap.isEmpty()) {
                            notFound.add(email);
                        } else {
                            String uid = snap.getDocuments().get(0).getId();
                            if (uid != null && !uid.trim().isEmpty()) {
                                memberUids.add(uid);
                            }
                        }

                        if (pending.decrementAndGet() == 0) {
                            finalizeCreateGroup(db, groupName, memberUids, notFound, myUid);
                        }
                    })
                    .addOnFailureListener(e -> {
                        notFound.add(email);
                        if (pending.decrementAndGet() == 0) {
                            finalizeCreateGroup(db, groupName, memberUids, notFound, myUid);
                        }
                    });
        }
    }

    private void finalizeCreateGroup(
            FirebaseFirestore db,
            String groupName,
            Set<String> memberUids,
            List<String> notFound,
            String myUid
    ) {
        if (!notFound.isEmpty()) {
            Toast.makeText(
                    this,
                    "Không tìm thấy: " + TextUtils.join(", ", notFound),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String chatId = db.collection("chats").document().getId();
        Timestamp now = Timestamp.now();
        Map<String, Object> data = new HashMap<>();
        data.put("type", "group");
        data.put("title", groupName);
        data.put("members", new ArrayList<>(memberUids));
        data.put("createdAt", now);
        data.put("lastMessage", "");
        data.put("lastTime", now);
        data.put("lastSenderId", "");
        data.put("lastSenderName", "");

        WriteBatch batch = db.batch();
        DocumentReference chatRef = db.collection("chats").document(chatId);
        batch.set(chatRef, data);

        for (String uid : memberUids) {
            if (uid == null || uid.trim().isEmpty()) continue;
            if (uid.equals(myUid)) continue;

            DocumentReference notiRef = db.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document();

            Map<String, Object> noti = new HashMap<>();
            noti.put("type", "group_add");
            noti.put("title", "Bạn đã được thêm vào nhóm");
            noti.put("content", groupName);
            noti.put("chatId", chatId);
            noti.put("createdAt", now);
            noti.put("isRead", false);

            batch.set(notiRef, noti);
        }

        batch.commit()
                .addOnSuccessListener(v -> {
                    Intent i = new Intent(this, ChatDetailActivity.class);
                    i.putExtra("chatId", chatId);
                    i.putExtra("chatName", groupName);
                    i.putExtra("isGroup", true);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Tạo nhóm lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
