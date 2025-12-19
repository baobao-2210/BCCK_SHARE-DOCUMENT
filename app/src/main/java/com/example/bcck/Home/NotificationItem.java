package com.example.bcck.Home;
// File: NotificationItem.java
public class NotificationItem {
    // Các biến thành viên (fields)
    private String title;
    private String content;
    private String time;
    private int iconResId;
    private boolean isRead;

    // Constructor (đã có trong code trước)
    public NotificationItem(String title, String content, String time, int iconResId, boolean isRead) {
        this.title = title;
        this.content = content;
        this.time = time;
        this.iconResId = iconResId;
        this.isRead = isRead;
    }

    // **********************************************
    // * PHẦN BỊ THIẾU CẦN THÊM VÀO *
    // **********************************************

    // 1. Getter cho title (Lỗi dòng 46)
    public String getTitle() {
        return title;
    }

    // 2. Getter cho content (Lỗi dòng 47)
    public String getContent() {
        return content;
    }

    // 3. Getter cho time (Lỗi dòng 48)
    public String getTime() {
        return time;
    }

    // 4. Getter cho iconResId (Lỗi dòng 49)
    public int getIconResId() {
        return iconResId;
    }

    // 5. Getter cho isRead (Lỗi dòng 52) - Thường dùng là isRead() cho boolean
    public boolean isRead() {
        return isRead;
    }

    // 6. Setter cho isRead (Đã được dùng trong NotificationActivity)
    public void setRead(boolean read) {
        isRead = read;
    }
}
