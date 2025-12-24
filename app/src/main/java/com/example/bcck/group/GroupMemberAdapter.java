package com.example.bcck.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberVH> {

    public interface OnRemoveClick {
        void onRemove(String email);
    }

    private final List<String> emails;
    private final OnRemoveClick onRemoveClick;

    public GroupMemberAdapter(List<String> emails, OnRemoveClick onRemoveClick) {
        this.emails = emails;
        this.onRemoveClick = onRemoveClick;
    }

    @NonNull
    @Override
    public MemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new MemberVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberVH holder, int position) {
        String email = emails.get(position);
        holder.tvEmail.setText(email);
        holder.btnRemove.setOnClickListener(v -> {
            if (onRemoveClick != null) onRemoveClick.onRemove(email);
        });
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    static class MemberVH extends RecyclerView.ViewHolder {
        TextView tvEmail;
        ImageView btnRemove;

        MemberVH(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            btnRemove = itemView.findViewById(R.id.btnRemoveMember);
        }
    }
}
