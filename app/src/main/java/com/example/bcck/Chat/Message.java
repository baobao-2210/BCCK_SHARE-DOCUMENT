package com.example.bcck.Chat;

public class Message {
    private String text;
    private String senderName;
    private String time;
    private boolean isSentByMe;

    public Message(String text, String senderName, String time, boolean isSentByMe) {
        this.text = text;
        this.senderName = senderName;
        this.time = time;
        this.isSentByMe = isSentByMe;
    }

    public String getText() { return text; }
    public String getSenderName() { return senderName; }
    public String getTime() { return time; }
    public boolean isSentByMe() { return isSentByMe; }
}