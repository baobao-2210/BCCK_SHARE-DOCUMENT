package com.example.bcck.Chat;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bcck.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;
    private OnChatClickListener clickListener;

    public interface OnChatClickListener {
        void onChatClick(ChatItem chatItem);
    }

    public ChatAdapter(List<ChatItem> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chat = chatList.get(position);

        holder.tvChatName.setText(chat.getChatName());
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());
        holder.avatarText.setText(chat.getAvatarText());
        bindAvatar(holder, chat);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatName, tvLastMessage, tvTime, avatarText;
        ImageView avatarImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            avatarText = itemView.findViewById(R.id.avatarText);
            avatarImage = itemView.findViewById(R.id.avatarImage);
        }
    }

    private void bindAvatar(ChatViewHolder holder, ChatItem chat) {
        String avatarUrl = chat.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            holder.avatarImage.setVisibility(View.GONE);
            holder.avatarText.setVisibility(View.VISIBLE);
            return;
        }

        holder.avatarText.setVisibility(View.GONE);
        holder.avatarImage.setVisibility(View.VISIBLE);

        if (avatarUrl.startsWith("http")) {
            Glide.with(holder.avatarImage.getContext())
                    .load(avatarUrl)
                    .circleCrop()
                    .into(holder.avatarImage);
            return;
        }

        try {
            byte[] imageBytes = Base64.decode(avatarUrl, Base64.DEFAULT);
            Glide.with(holder.avatarImage.getContext())
                    .load(imageBytes)
                    .circleCrop()
                    .into(holder.avatarImage);
        } catch (IllegalArgumentException e) {
            holder.avatarImage.setVisibility(View.GONE);
            holder.avatarText.setVisibility(View.VISIBLE);
        }
    }
}
