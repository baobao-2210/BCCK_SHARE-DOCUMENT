package com.example.bcck.Home;

public class NotificationItem {

    private final String id;
    private final String chatId;
    private final String title;
    private final String content;
    private final String time;     // đã format sẵn để hiển thị
    private final int iconResId;
    private boolean isRead;

    public NotificationItem(String id,
                            String chatId,
                            String title,
                            String content,
                            String time,
                            int iconResId,
                            boolean isRead) {
        this.id = id;
        this.chatId = chatId;
        this.title = title;
        this.content = content;
        this.time = time;
        this.iconResId = iconResId;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
