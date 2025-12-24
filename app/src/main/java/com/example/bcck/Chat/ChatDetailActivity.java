package com.example.bcck.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDebug";

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new java.util.ArrayList<>();

    private EditText etMessage;
    private ImageView btnSend;

    private FirebaseFirestore db;
    private String myUid;
    private String myName = "Me";

    private String chatId;
    private String receiverId;
    private String receiverName;
    private String chatName;
    private boolean isGroup;
    private final List<String> groupMemberIds = new ArrayList<>();

    private ListenerRegistration msgListener;
    private boolean firstLoad = true;

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

        // extras
        chatId = getIntent().getStringExtra("chatId");
        chatName = getIntent().getStringExtra("chatName");
        isGroup = getIntent().getBooleanExtra("isGroup", false);
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        TextView tvTitle = findViewById(R.id.tvChatDetailTitle);
        ImageView btnBack = findViewById(R.id.btnBackChatDetail);
        ImageView btnDeleteGroup = findViewById(R.id.btnDeleteGroup);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        btnBack.setOnClickListener(v -> finish());
        if (btnDeleteGroup != null) {
            btnDeleteGroup.setVisibility(isGroup ? View.VISIBLE : View.GONE);
            btnDeleteGroup.setOnClickListener(v -> confirmDeleteGroup());
        }

        setupRecyclerView();

        loadMyName(() -> {
            if (isGroup) {
                String title = (chatName != null && !chatName.trim().isEmpty()) ? chatName : "Nhóm chat";
                tvTitle.setText(title);

                if (chatId == null || chatId.trim().isEmpty()) {
                    chatId = buildGroupId(title);
                }

                ensureGroupChatExists(chatId, title, () -> {
                    loadGroupMembers(chatId);
                    startListenMessages(chatId);
                });
                return;
            }

            // ưu tiên: nếu có chatId thì mở luôn
            if (chatId != null && !chatId.trim().isEmpty()) {
                String chatName = getIntent().getStringExtra("chatName");
                tvTitle.setText(chatName != null && !chatName.trim().isEmpty() ? chatName : "Tin nhắn");
                startListenMessages(chatId);
                return;
            }

            // nếu không có chatId, nhưng có receiverId => tìm/tạo direct chat chuẩn
            if (receiverId != null && !receiverId.trim().isEmpty()) {
                tvTitle.setText(receiverName != null && !receiverName.trim().isEmpty() ? receiverName : "Tin nhắn");
                findOrCreateDirectChat(receiverId, id -> {
                    chatId = id;
                    startListenMessages(chatId);
                });
                return;
            }

            Toast.makeText(this, "Thiếu thông tin chat", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private String buildGroupId(String groupName) {
        String base = groupName == null ? "" : groupName.trim().toLowerCase(Locale.ROOT);
        return "group_" + Integer.toHexString(base.hashCode());
    }

    private void ensureGroupChatExists(String id, String title, Runnable done) {
        if (id == null || id.trim().isEmpty()) {
            done.run();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", "group");
        if (title != null) data.put("title", title);
        data.put("createdAt", Timestamp.now());
        if (myUid != null && !myUid.trim().isEmpty()) {
            data.put("members", FieldValue.arrayUnion(myUid));
        }

        db.collection("chats").document(id)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(v -> done.run())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không tạo được nhóm chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    done.run();
                });
    }

    private void loadGroupMembers(String id) {
        if (id == null || id.trim().isEmpty()) return;
        db.collection("chats").document(id).get()
                .addOnSuccessListener(doc -> {
                    List<String> members = (List<String>) doc.get("members");
                    groupMemberIds.clear();
                    if (members != null) groupMemberIds.addAll(members);
                });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messageList);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);

        recyclerViewMessages.setLayoutManager(lm);
        recyclerViewMessages.setHasFixedSize(true);
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void loadMyName(Runnable done) {
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(doc -> {
                    String fullName = doc.getString("fullName");
                    String email = doc.getString("email");

                    if (fullName != null && !fullName.trim().isEmpty()) myName = fullName.trim();
                    else if (email != null && !email.trim().isEmpty()) myName = email.trim();
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

    private interface ChatIdCallback {
        void onChatId(String chatId);
    }

    private void findOrCreateDirectChat(String otherUid, ChatIdCallback cb) {
        String stableId = buildStableDirectChatId(myUid, otherUid);

        db.collection("chats").document(stableId).get()
                .addOnSuccessListener(stableDoc -> {
                    if (stableDoc.exists()) {
                        Log.d(TAG, "Use stable chat: " + stableId);
                        cb.onChatId(stableId);
                        return;
                    }

                    // fallback: tìm chat direct cũ random id để tránh tách hội thoại
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
                                    return;
                                }

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
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Không tạo được chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi tìm chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi mở chat: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * ✅ Tối ưu:
     * - limitToLast(80): chỉ lấy 80 tin gần nhất
     * - Lần đầu: build list từ snapshot
     * - Các lần sau: chỉ append DocumentChange.ADDED
     */
    private void startListenMessages(String chatId) {
        if (msgListener != null) msgListener.remove();

        Query q = db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(80);

        firstLoad = true;

        msgListener = q.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Log.e(TAG, "listen messages error", e);
                return;
            }
            if (snap == null) return;

            if (firstLoad) {
                messageList.clear();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    messageList.add(Message.fromDoc(d, myUid));
                }
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
                firstLoad = false;
                return;
            }

            for (DocumentChange dc : snap.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    messageList.add(Message.fromDoc(dc.getDocument(), myUid));
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                }
            }
        });
    }
    private void resolveReceiverIdFromChatIfNeeded(String chatId, Runnable done) {
        if (receiverId != null && !receiverId.trim().isEmpty()) {
            done.run();
            return;
        }

        db.collection("chats").document(chatId).get()
                .addOnSuccessListener(doc -> {
                    List<String> members = (List<String>) doc.get("members");
                    if (members != null) {
                        for (String uid : members) {
                            if (uid != null && !uid.equals(myUid)) {
                                receiverId = uid;
                                break;
                            }
                        }
                    }
                    done.run();
                })
                .addOnFailureListener(e -> done.run());
    }

    private void sendMessage() {
        if (chatId == null || chatId.trim().isEmpty()) {
            Toast.makeText(this, "Chưa sẵn sàng để gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        final String messageText = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (messageText.isEmpty()) return;

        final Timestamp now = Timestamp.now();

        // data message
        final Map<String, Object> msg = new HashMap<>();
        msg.put("text", messageText);
        msg.put("senderId", myUid);
        msg.put("senderName", myName);
        msg.put("createdAt", now);

        // update chat
        final Map<String, Object> chatUpdate = new HashMap<>();
        chatUpdate.put("lastMessage", messageText);
        chatUpdate.put("lastTime", now);
        chatUpdate.put("lastSenderId", myUid);
        chatUpdate.put("lastSenderName", myName);

        final DocumentReference chatRef = db.collection("chats").document(chatId);
        final DocumentReference msgRef = chatRef.collection("messages").document();

        // UX: clear ngay
        etMessage.setText("");

        if (isGroup) {
            sendGroupMessage(chatRef, msgRef, msg, chatUpdate, messageText);
            return;
        }

        // đảm bảo có receiverId rồi mới tạo notification
        resolveReceiverIdFromChatIfNeeded(chatId, () -> {
            WriteBatch batch = db.batch();

            batch.set(msgRef, msg);
            batch.update(chatRef, chatUpdate);

            // ✅ tạo notification cho người nhận (in-app)
            if (receiverId != null && !receiverId.trim().isEmpty()) {
                DocumentReference notiRef = db.collection("users")
                        .document(receiverId)
                        .collection("notifications")
                        .document(); // auto id

                Map<String, Object> noti = new HashMap<>();
                noti.put("type", "message");
                noti.put("title", "Tin nhắn mới");
                noti.put("content", myName + ": " + messageText);
                noti.put("chatId", chatId);
                noti.put("createdAt", now);
                noti.put("isRead", false);

                batch.set(notiRef, noti);
            }

            batch.commit().addOnFailureListener(e ->
                    Toast.makeText(this, "Gửi lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void sendGroupMessage(
            DocumentReference chatRef,
            DocumentReference msgRef,
            Map<String, Object> msg,
            Map<String, Object> chatUpdate,
            String messageText
    ) {
        Runnable commit = () -> {
            WriteBatch batch = db.batch();
            batch.set(msgRef, msg);
            batch.set(chatRef, chatUpdate, SetOptions.merge());

            String title = (chatName != null && !chatName.trim().isEmpty()) ? chatName : "Nhóm chat";
            String content = myName + ": " + messageText;

            for (String uid : groupMemberIds) {
                if (uid == null || uid.trim().isEmpty()) continue;
                if (uid.equals(myUid)) continue;

                DocumentReference notiRef = db.collection("users")
                        .document(uid)
                        .collection("notifications")
                        .document();

                Map<String, Object> noti = new HashMap<>();
                noti.put("type", "group_message");
                noti.put("title", title);
                noti.put("content", content);
                noti.put("chatId", chatId);
                noti.put("createdAt", Timestamp.now());
                noti.put("isRead", false);

                batch.set(notiRef, noti);
            }

            batch.commit().addOnFailureListener(e ->
                    Toast.makeText(this, "Gửi lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        };

        if (!groupMemberIds.isEmpty()) {
            commit.run();
            return;
        }

        db.collection("chats").document(chatId).get()
                .addOnSuccessListener(doc -> {
                    List<String> members = (List<String>) doc.get("members");
                    groupMemberIds.clear();
                    if (members != null) groupMemberIds.addAll(members);
                    commit.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gửi lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmDeleteGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhóm chat")
                .setMessage("Bạn có chắc muốn xóa nhóm này?")
                .setPositiveButton("Xóa", (d, w) -> deleteGroupChat())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteGroupChat() {
        if (chatId == null || chatId.trim().isEmpty()) return;
        deleteGroupMessagesThenChat(chatId);
    }

    private void deleteGroupMessagesThenChat(@NonNull String id) {
        db.collection("chats").document(id)
                .collection("messages")
                .limit(200)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        db.collection("chats").document(id)
                                .delete()
                                .addOnSuccessListener(v -> finish())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Xóa nhóm lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        batch.delete(d.getReference());
                    }
                    batch.commit().addOnSuccessListener(v -> deleteGroupMessagesThenChat(id))
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Xóa nhóm lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Xóa nhóm lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msgListener != null) msgListener.remove();
    }
}
