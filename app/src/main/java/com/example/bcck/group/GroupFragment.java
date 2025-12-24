package com.example.bcck.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private RecyclerView recyclerViewGroups;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;
    private List<Group> filteredGroupList;

    private EditText searchBox;
    private ImageView addIcon, btnBack;
    private Button btnMyGroups, btnDiscover;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadGroupData();

        return view;
    }

    private void initViews(View view) {
        recyclerViewGroups = view.findViewById(R.id.recyclerViewGroups);
        searchBox = view.findViewById(R.id.searchBox);
        addIcon = view.findViewById(R.id.addIcon);
        btnBack = view.findViewById(R.id.btnBack);
        btnMyGroups = view.findViewById(R.id.btnMyGroups);
        btnDiscover = view.findViewById(R.id.btnDiscover);
    }

    private void setupRecyclerView() {
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        filteredGroupList = new ArrayList<>();

        groupAdapter = new GroupAdapter(filteredGroupList, group -> {
            // Khi nhấn nút Chat → Chuyển sang ChatActivity
            Intent intent = new Intent(getActivity(), ChatDetailActivity.class);
            String chatId = group.getGroupId();
            if (chatId == null || chatId.trim().isEmpty()) {
                chatId = buildGroupId(group.getGroupName());
            }
            intent.putExtra("chatId", chatId);
            intent.putExtra("chatName", group.getGroupName());
            intent.putExtra("isGroup", true);
            intent.putExtra("groupName", group.getGroupName());
            intent.putExtra("memberCount", group.getMemberCount());
            startActivity(intent);
        });

        recyclerViewGroups.setAdapter(groupAdapter);
    }

    private void setupSearch() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGroups(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateGroupActivity.class);
            startActivity(intent);
        });
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            // Quay lại màn hình trước
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnMyGroups.setOnClickListener(v -> {
            selectFilter(btnMyGroups);
            loadMyGroups();
        });

        btnDiscover.setOnClickListener(v -> {
            selectFilter(btnDiscover);
            loadDiscoverGroups();
        });
    }

    private void selectFilter(Button selectedButton) {
        // Reset tất cả
        btnMyGroups.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        btnMyGroups.setTextColor(0xFF000000);
        btnDiscover.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        btnDiscover.setTextColor(0xFF000000);

        // Highlight button được chọn
        selectedButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark, null));
        selectedButton.setTextColor(0xFFFFFFFF);
    }

    private void loadGroupData() {
        loadMyGroups();
    }

    private void loadMyGroups() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("chats")
                .whereEqualTo("type", "group")
                .whereArrayContains("members", myUid)
                .get()
                .addOnSuccessListener(snap -> {
                    groupList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String id = d.getId();
                        String title = d.getString("title");
                        if (title == null || title.trim().isEmpty()) title = "Nhóm chat";

                        List<String> members = (List<String>) d.get("members");
                        int memberCount = members != null ? members.size() : 0;

                        groupList.add(new Group(id, title, memberCount));
                    }

                    filteredGroupList.clear();
                    filteredGroupList.addAll(groupList);
                    groupAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi load nhóm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadDiscoverGroups() {
        // Load nhóm khám phá
        Toast.makeText(getContext(), "Hiển thị nhóm khám phá", Toast.LENGTH_SHORT).show();
        // TODO: Call API để lấy nhóm khám phá
    }

    private void filterGroups(String query) {
        filteredGroupList.clear();

        if (query.isEmpty()) {
            filteredGroupList.addAll(groupList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Group group : groupList) {
                if (group.getGroupName().toLowerCase().contains(lowerQuery)) {
                    filteredGroupList.add(group);
                }
            }
        }

        groupAdapter.notifyDataSetChanged();

    }

    private String buildGroupId(String groupName) {
        String base = groupName == null ? "" : groupName.trim().toLowerCase(Locale.ROOT);
        return "group_" + Integer.toHexString(base.hashCode());
    }
}
