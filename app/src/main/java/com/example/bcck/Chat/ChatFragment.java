package com.example.bcck.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.Home.NotificationActivity;
import com.example.bcck.HomeActivity;
import com.example.bcck.Profile.ProfileFragment;
import com.example.bcck.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewChats;
    private ChatAdapter chatAdapter;
    private final List<ChatItem> chatList = new ArrayList<>();
    private final List<ChatItem> filteredChatList = new ArrayList<>();

    private EditText searchBox;
    private ImageView btnBack, iconSettings, iconNotification, iconProfile;

    private ListenerRegistration chatListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        searchBox = view.findViewById(R.id.searchBox);
        btnBack = view.findViewById(R.id.btnBack);
        iconSettings = view.findViewById(R.id.iconSettings);
        iconNotification = view.findViewById(R.id.iconNotification);
        iconProfile = view.findViewById(R.id.iconProfile);

        setupRecyclerView();
        setupSearch();
        setupButtons();
        listenChatsFromFirestore();

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        chatAdapter = new ChatAdapter(filteredChatList, chatItem -> {
            Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
            intent.putExtra("chatId", chatItem.getChatId());
            intent.putExtra("chatName", chatItem.getChatName());
            intent.putExtra("isGroup", chatItem.isGroup());
            intent.putExtra("RECEIVER_ID", chatItem.getOtherUserId());
            intent.putExtra("RECEIVER_NAME", chatItem.getChatName());
            startActivity(intent);
        });

        recyclerViewChats.setAdapter(chatAdapter);
    }

    private void listenChatsFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("CHAT", "User is null");
            return;
        }

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (chatListener != null) chatListener.remove();

        chatListener = db.collection("chats")
                .whereArrayContains("members", myUid)
                .orderBy("lastTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("CHAT", "listenChats error", e);
                        return;
                    }
                    if (snap == null) return;

                    chatList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String chatId = d.getId();

                        String type = d.getString("type");
                        boolean isGroup = "group".equals(type);

                        String title = d.getString("title");
                        if (title == null) title = "";

                        String lastMessage = d.getString("lastMessage");
                        if (lastMessage == null) lastMessage = "";

                        String lastSenderId = d.getString("lastSenderId");
                        if (lastSenderId != null && lastSenderId.equals(myUid) && !lastMessage.isEmpty()) {
                            lastMessage = "Bạn: " + lastMessage;
                        }

                        String time = "";
                        Timestamp ts = d.getTimestamp("lastTime");
                        if (ts != null) {
                            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts.toDate());
                        }

                        String otherUserId = null;
                        List<String> members = (List<String>) d.get("members");
                        if (!isGroup && members != null && members.size() >= 2) {
                            for (String m : members) {
                                if (m != null && !m.equals(myUid)) {
                                    otherUserId = m;
                                    break;
                                }
                            }
                        }

                        String showName;
                        if (isGroup) {
                            showName = title.isEmpty() ? "Nhóm chat" : title;
                        } else {
                            showName = "Đang tải...";
                        }

                        chatList.add(new ChatItem(
                                chatId,
                                showName,
                                lastMessage,
                                time,
                                makeAvatarText(showName),
                                isGroup,
                                otherUserId
                        ));
                    }

                    applyCurrentFilter();

                    // quan trọng: resolve tên user bên kia cho direct chat
                    resolveDirectNames(myUid);

                    Log.d("CHAT", "snap size=" + snap.size());
                });
    }

    private void resolveDirectNames(String myUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (int i = 0; i < chatList.size(); i++) {
            ChatItem item = chatList.get(i);
            if (item.isGroup()) continue;
            String otherUid = item.getOtherUserId();
            if (otherUid == null || otherUid.isEmpty()) continue;

            final int index = i;

            db.collection("users").document(otherUid).get()
                    .addOnSuccessListener(userDoc -> {
                        String fullName = userDoc.getString("fullName");
                        String email = userDoc.getString("email");

                        String display = (fullName != null && !fullName.trim().isEmpty())
                                ? fullName.trim()
                                : (email != null ? email : "Người dùng");

                        item.setChatName(display);
                        item.setAvatarText(makeAvatarText(display));

                        applyCurrentFilter(); // để list đang filter vẫn update đúng
                    });
        }
    }

    private void setupSearch() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyCurrentFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyCurrentFilter() {
        String query = searchBox.getText() == null ? "" : searchBox.getText().toString().trim();
        filteredChatList.clear();

        if (query.isEmpty()) {
            filteredChatList.addAll(chatList);
        } else {
            String q = query.toLowerCase();
            for (ChatItem c : chatList) {
                String name = c.getChatName() == null ? "" : c.getChatName();
                String msg = c.getLastMessage() == null ? "" : c.getLastMessage();
                if (name.toLowerCase().contains(q) || msg.toLowerCase().contains(q)) {
                    filteredChatList.add(c);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            // Chuyển từ màn hình hiện tại về HomeActivity
            Intent intent = new Intent(getContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        });

        iconSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chọn người để chat", Toast.LENGTH_SHORT).show();
            // nếu bạn có SelectUserActivity thì mở ở đây
            // startActivity(new Intent(getActivity(), SelectUserActivity.class));
        });

        iconNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NotificationActivity.class);
            startActivity(intent);
        });


        iconProfile.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity activity = (HomeActivity) getActivity();
                activity.loadFragment(new ProfileFragment(), 4);
                activity.updateBottomNav(4);
            }
        });


    }

    private String makeAvatarText(String title) {
        String t = title == null ? "" : title.trim();
        if (t.length() >= 2) return t.substring(0, 2).toUpperCase();
        if (t.length() == 1) return t.substring(0, 1).toUpperCase();
        return "CH";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListener != null) chatListener.remove();
    }
}
