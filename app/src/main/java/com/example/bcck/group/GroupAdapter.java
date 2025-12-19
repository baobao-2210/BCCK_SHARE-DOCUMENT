package com.example.bcck.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList;
    private OnChatClickListener chatClickListener;

    public interface OnChatClickListener {
        void onChatClick(Group group);
    }

    public GroupAdapter(List<Group> groupList, OnChatClickListener listener) {
        this.groupList = groupList;
        this.chatClickListener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        holder.tvGroupName.setText(group.getGroupName());
        holder.tvMemberCount.setText(group.getMemberCount() + " thành viên");

        holder.btnChat.setOnClickListener(v -> {
            if (chatClickListener != null) {
                chatClickListener.onChatClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvMemberCount;
        Button btnChat;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvMemberCount = itemView.findViewById(R.id.tvMemberCount);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}