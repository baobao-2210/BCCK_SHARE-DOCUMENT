package com.example.bcck.Chat;

public class ChatItem {
    private String chatId;
    private String chatName;
    private String lastMessage;
    private String time;
    private String avatarText;
    private boolean isGroup;

    // dùng để biết user bên kia (direct chat)
    private String otherUserId;

    public ChatItem() {}

    public ChatItem(String chatId, String chatName, String lastMessage, String time,
                    String avatarText, boolean isGroup, String otherUserId) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.lastMessage = lastMessage;
        this.time = time;
        this.avatarText = avatarText;
        this.isGroup = isGroup;
        this.otherUserId = otherUserId;
    }

    public String getChatId() { return chatId; }
    public String getChatName() { return chatName; }
    public String getLastMessage() { return lastMessage; }
    public String getTime() { return time; }
    public String getAvatarText() { return avatarText; }
    public boolean isGroup() { return isGroup; }
    public String getOtherUserId() { return otherUserId; }

    public void setChatName(String chatName) { this.chatName = chatName; }
    public void setAvatarText(String avatarText) { this.avatarText = avatarText; }
}
