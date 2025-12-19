package com.example.bcck.Chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "CHAT_DETAIL";

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();

    private EditText etMessage;
    private ImageView btnSend;

    private LinearLayoutManager layoutManager;

    private String chatId;
    private String chatName;

    private ListenerRegistration msgListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        chatId = getIntent().getStringExtra("chatId");
        chatName = getIntent().getStringExtra("chatName");

        TextView tvTitle = findViewById(R.id.tvChatDetailTitle);
        ImageView btnBack = findViewById(R.id.btnBackChatDetail);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        tvTitle.setText(chatName == null ? "Chat" : chatName);
        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();

        if (TextUtils.isEmpty(chatId)) {
            Log.e(TAG, "chatId is null or empty");
            return;
        }

        listenMessages();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);

        Log.d(TAG, "RecyclerView setup complete");
    }

    private void listenMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (msgListener != null) msgListener.remove();

        msgListener = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e(TAG, "listenMessages error", e);
                        return;
                    }
                    if (snap == null) return;

                    String myUid = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                            : "";

                    messageList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String text = d.getString("text");
                        String senderId = d.getString("senderId");
                        String senderName = d.getString("senderName");

                        boolean isMe = myUid.equals(senderId);

                        String time = "";
                        Timestamp ts = d.getTimestamp("createdAt");
                        if (ts != null) {
                            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts.toDate());
                        }

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

                    Log.d(TAG, "Messages loaded: " + messageList.size());
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User null, chưa đăng nhập");
            return;
        }

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String myName = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // tạm dùng email làm tên

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> msg = new HashMap<>();
        msg.put("text", messageText);
        msg.put("senderId", myUid);
        msg.put("senderName", myName == null ? "Me" : myName);
        msg.put("createdAt", Timestamp.now());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Message sent: " + ref.getId());
                    etMessage.setText("");

                    // update lastMessage ở document chat
                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", messageText);
                    update.put("lastSenderId", myUid);
                    update.put("lastTime", Timestamp.now());

                    db.collection("chats").document(chatId).update(update);
                })
                .addOnFailureListener(e -> Log.e(TAG, "sendMessage failed", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msgListener != null) msgListener.remove();
    }
}
