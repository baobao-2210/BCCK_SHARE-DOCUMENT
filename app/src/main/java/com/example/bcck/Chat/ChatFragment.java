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

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupButtons();
        listenChatsFromFirestore();

        return view;
    }

    private void initViews(View view) {
        recyclerViewChats = view.findViewById(R.id.recyclerViewChats);
        searchBox = view.findViewById(R.id.searchBox);
        btnBack = view.findViewById(R.id.btnBack);
        iconSettings = view.findViewById(R.id.iconSettings);
        iconNotification = view.findViewById(R.id.iconNotification);
        iconProfile = view.findViewById(R.id.iconProfile);
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        chatAdapter = new ChatAdapter(filteredChatList, chatItem -> {
            if (chatItem.getChatId() == null || chatItem.getChatId().isEmpty()) return;

            Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
            intent.putExtra("chatId", chatItem.getChatId());
            intent.putExtra("chatName", chatItem.getChatName());
            intent.putExtra("isGroup", chatItem.isGroup());
            startActivity(intent);
        });

        recyclerViewChats.setAdapter(chatAdapter);
    }

    private void listenChatsFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e("CHAT", "User is null, chưa đăng nhập");
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d("CHAT", "My uid = " + uid);
        Toast.makeText(getContext(), "UID: " + uid, Toast.LENGTH_SHORT).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (chatListener != null) chatListener.remove();

        chatListener = db.collection("chats")
                .whereArrayContains("members", uid)
                .orderBy("lastTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Log.e("CHAT", "listenChats error", e);
                        Toast.makeText(getContext(), "Firestore lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snap == null) return;

                    Log.d("CHAT", "snap size = " + snap.size());
                    Toast.makeText(getContext(), "Chats: " + snap.size(), Toast.LENGTH_SHORT).show();

                    chatList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String chatId = d.getId();

                        String title = d.getString("title");
                        if (title == null || title.trim().isEmpty()) title = "Chat";

                        String lastMessage = d.getString("lastMessage");
                        if (lastMessage == null) lastMessage = "";

                        boolean isGroup = "group".equals(d.getString("type"));

                        String time = "";
                        Timestamp ts = d.getTimestamp("lastTime");
                        if (ts != null) {
                            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(ts.toDate());
                        }

                        String avatarText = makeAvatarText(title);

                        chatList.add(new ChatItem(chatId, title, lastMessage, time, avatarText, isGroup));
                    }

                    applyCurrentFilter();
                });
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
        filterChats(query);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        iconSettings.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), SelectUserActivity.class);
            startActivity(i);
        });

        iconNotification.setOnClickListener(v ->
                Toast.makeText(getContext(), "Thông báo", Toast.LENGTH_SHORT).show()
        );

        iconProfile.setOnClickListener(v ->
                Toast.makeText(getContext(), "Cá nhân", Toast.LENGTH_SHORT).show()
        );
    }

    private void filterChats(String query) {
        filteredChatList.clear();

        if (query.isEmpty()) {
            filteredChatList.addAll(chatList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ChatItem chat : chatList) {
                String name = chat.getChatName() == null ? "" : chat.getChatName();
                String msg = chat.getLastMessage() == null ? "" : chat.getLastMessage();

                if (name.toLowerCase().contains(lowerQuery) || msg.toLowerCase().contains(lowerQuery)) {
                    filteredChatList.add(chat);
                }
            }
        }

        chatAdapter.notifyDataSetChanged();
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
