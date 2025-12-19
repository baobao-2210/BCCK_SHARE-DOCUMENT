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

import com.example.bcck.Chat.ChatActivity;
import com.example.bcck.R;

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
            Intent intent = new Intent(getActivity(), ChatActivity.class);
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
            Toast.makeText(getContext(), "Tạo nhóm mới", Toast.LENGTH_SHORT).show();
            // TODO: Mở màn hình tạo nhóm
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
        // Dữ liệu mẫu
        groupList.clear();
        groupList.add(new Group("công nghệ phần mềm", 38));
        groupList.add(new Group("lập trình di động", 38));
        groupList.add(new Group("cơ sở dữ liệu", 25));
        groupList.add(new Group("mạng máy tính", 42));
        groupList.add(new Group("trí tuệ nhân tạo", 31));

        filteredGroupList.clear();
        filteredGroupList.addAll(groupList);
        groupAdapter.notifyDataSetChanged();
    }

    private void loadMyGroups() {
        // Load nhóm của tôi
        Toast.makeText(getContext(), "Hiển thị nhóm của tôi", Toast.LENGTH_SHORT).show();
        loadGroupData();
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
}
