package com.example.bcck.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnUserClick {
        void onClick(UserItem u);
    }

    private final List<UserItem> list;
    private final OnUserClick listener;

    public UserAdapter(List<UserItem> list, OnUserClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserItem u = list.get(position);
        holder.tvName.setText(u.fullName == null || u.fullName.isEmpty() ? "User" : u.fullName);
        holder.tvEmail.setText(u.email == null ? "" : u.email);
        holder.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}
