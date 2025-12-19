package com.example.bcck.Chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDebug";

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();
    private EditText etMessage;
    private ImageView btnSend;

    private FirebaseFirestore db;
    private String myUid;
    private String myName = "";

    private String chatId;
    private String receiverId;
    private String receiverName;

    private ListenerRegistration msgListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        db = FirebaseFirestore.getInstance();
        myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (myUid == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatId = getIntent().getStringExtra("chatId");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        TextView tvTitle = findViewById(R.id.tvChatDetailTitle);
        ImageView btnBack = findViewById(R.id.btnBackChatDetail);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();

        loadMyName(() -> {
            if (chatId != null && !chatId.isEmpty()) {
                tvTitle.setText(getIntent().getStringExtra("chatName") != null ? getIntent().getStringExtra("chatName") : "Tin nhắn");
                startListenMessages(chatId);
            } else if (receiverId != null && !receiverId.isEmpty()) {
                tvTitle.setText(receiverName != null && !receiverName.isEmpty() ? receiverName : "Tin nhắn");
                findOrCreateDirectChat(receiverId, id -> {
                    chatId = id;
                    startListenMessages(chatId);
                });
            } else {
                Toast.makeText(this, "Thiếu thông tin chat", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(lm);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void loadMyName(Runnable done) {
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(doc -> {
                    String fullName = doc.getString("fullName");
                    String email = doc.getString("email");
                    if (fullName != null && !fullName.trim().isEmpty()) myName = fullName.trim();
                    else if (email != null) myName = email;
                    else myName = "Me";
                    done.run();
                })
                .addOnFailureListener(e -> {
                    myName = "Me";
                    done.run();
                });
    }

    private String buildStableDirectChatId(String uidA, String uidB) {
        if (uidA.compareTo(uidB) < 0) return "direct_" + uidA + "_" + uidB;
        return "direct_" + uidB + "_" + uidA;
    }

    private interface ChatIdCallback { void onChatId(String chatId); }

    private void findOrCreateDirectChat(String otherUid, ChatIdCallback cb) {
        String stableId = buildStableDirectChatId(myUid, otherUid);

        db.collection("chats").document(stableId).get()
                .addOnSuccessListener(stableDoc -> {
                    if (stableDoc.exists()) {
                        Log.d(TAG, "Use stable chat: " + stableId);
                        cb.onChatId(stableId);
                    } else {
                        // tìm chat cũ đã tồn tại (random id) để không bị tách hội thoại
                        db.collection("chats")
                                .whereArrayContains("members", myUid)
                                .whereEqualTo("type", "direct")
                                .get()
                                .addOnSuccessListener(snap -> {
                                    String foundId = null;
                                    for (DocumentSnapshot d : snap.getDocuments()) {
                                        List<String> members = (List<String>) d.get("members");
                                        if (members != null && members.contains(otherUid)) {
                                            foundId = d.getId();
                                            break;
                                        }
                                    }

                                    if (foundId != null) {
                                        Log.d(TAG, "Found existing direct chat: " + foundId);
                                        cb.onChatId(foundId);
                                    } else {
                                        Log.d(TAG, "Create new stable direct chat: " + stableId);

                                        Map<String, Object> chatData = new HashMap<>();
                                        chatData.put("type", "direct");
                                        chatData.put("members", Arrays.asList(myUid, otherUid));
                                        chatData.put("title", "");
                                        chatData.put("lastMessage", "");
                                        chatData.put("lastTime", Timestamp.now());
                                        chatData.put("lastSenderId", "");
                                        chatData.put("lastSenderName", "");
                                        chatData.put("createdAt", Timestamp.now());

                                        db.collection("chats").document(stableId)
                                                .set(chatData)
                                                .addOnSuccessListener(v -> cb.onChatId(stableId))
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Không tạo được chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tìm chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi mở chat: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void startListenMessages(String chatId) {
        if (msgListener != null) msgListener.remove();

        msgListener = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "listen messages error", e);
                        return;
                    }
                    if (snap == null) return;

                    messageList.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String text = d.getString("text");
                        String senderId = d.getString("senderId");
                        String senderName = d.getString("senderName");
                        Timestamp createdAt = d.getTimestamp("createdAt");

                        boolean isMe = senderId != null && senderId.equals(myUid);

                        String time = "";
                        if (createdAt != null) {
                            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(createdAt.toDate());
                        }

                        // Message(text, senderName, time, isMe)
                        messageList.add(new Message(
                                text == null ? "" : text,
                                senderName == null ? "" : senderName,
                                time,
                                isMe
                        ));
                    }

                    messageAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Chưa sẵn sàng để gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageText = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (messageText.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("text", messageText);
        msg.put("senderId", myUid);
        msg.put("senderName", myName);
        msg.put("createdAt", Timestamp.now());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(ref -> {
                    etMessage.setText("");

                    Map<String, Object> chatUpdate = new HashMap<>();
                    chatUpdate.put("lastMessage", messageText);
                    chatUpdate.put("lastTime", Timestamp.now());
                    chatUpdate.put("lastSenderId", myUid);
                    chatUpdate.put("lastSenderName", myName);

                    db.collection("chats").document(chatId).update(chatUpdate);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gửi lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msgListener != null) msgListener.remove();
    }
}
