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

import java.util.ArrayList;
import java.util.List;

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
                        if (uid == null || uid.equals(myUid)) continue;

                        String fullName = d.getString("fullName");
                        String email = d.getString("email");

                        users.add(new UserItem(uid, fullName, email));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("USERS", "loadUsers fail", e));
    }

    private void openDirectChat(UserItem other) {
        if (other == null || other.uid == null || other.uid.trim().isEmpty()) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // không cho chat với chính mình
        if (other.uid.equals(myUid)) return;

        String receiverName =
                (other.fullName != null && !other.fullName.trim().isEmpty())
                        ? other.fullName.trim()
                        : (other.email != null && !other.email.trim().isEmpty() ? other.email.trim() : "Người dùng");

        // ✅ QUAN TRỌNG: chỉ gửi RECEIVER_ID/RECEIVER_NAME
        // ChatDetailActivity sẽ tự findOrCreateDirectChat() theo stable chatId direct_uidA_uidB
        Intent i = new Intent(this, ChatDetailActivity.class);
        i.putExtra("RECEIVER_ID", other.uid);
        i.putExtra("RECEIVER_NAME", receiverName);
        startActivity(i);
    }
}
