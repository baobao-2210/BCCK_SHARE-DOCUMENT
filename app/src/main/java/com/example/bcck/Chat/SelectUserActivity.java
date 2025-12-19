package com.example.bcck.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectUserActivity extends AppCompatActivity {

    private final List<UserItem> users = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        RecyclerView rv = findViewById(R.id.recyclerUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserAdapter(users, this::openDirectChat);
        rv.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(snap -> {
                    users.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String uid = d.getId();
                        if (uid.equals(myUid)) continue;

                        String fullName = d.getString("fullName");
                        String email = d.getString("email");

                        users.add(new UserItem(uid, fullName, email));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("USERS", "loadUsers fail", e));
    }

    private String directChatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    private void openDirectChat(UserItem other) {
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String chatId = directChatId(myUid, other.uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String title = (other.fullName != null && !other.fullName.trim().isEmpty())
                ? other.fullName
                : (other.email != null ? other.email : "Chat");

        Map<String, Object> chat = new HashMap<>();
        chat.put("type", "direct");
        chat.put("members", Arrays.asList(myUid, other.uid));
        chat.put("title", title);
        chat.put("lastMessage", "");
        chat.put("lastSenderId", "");
        chat.put("lastTime", com.google.firebase.Timestamp.now());

        db.collection("chats").document(chatId)
                .set(chat, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    Intent i = new Intent(this, ChatDetailActivity.class);
                    i.putExtra("chatId", chatId);
                    i.putExtra("chatName", title);
                    startActivity(i);
                })
                .addOnFailureListener(e -> Log.e("CHAT", "create chat fail", e));
    }
}
