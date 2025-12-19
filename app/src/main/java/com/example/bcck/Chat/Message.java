package com.example.bcck.Chat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Message {
    private String text;
    private String senderId;
    private String senderName;
    private Timestamp createdAt;
    private boolean sentByMe;

    public Message() {}

    public Message(String text, String senderName, String time, boolean sentByMe) {
        this.text = text;
        this.senderName = senderName;
        this.sentByMe = sentByMe;
        this.createdAt = null;
    }

    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isSentByMe() { return sentByMe; }

    public String getTime() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(createdAt.toDate());
    }

    public static Message fromDoc(DocumentSnapshot d, String myUid) {
        Message m = new Message();
        m.text = d.getString("text");
        m.senderId = d.getString("senderId");
        m.senderName = d.getString("senderName");
        m.createdAt = d.getTimestamp("createdAt");
        m.sentByMe = (myUid != null && myUid.equals(m.senderId));
        return m;
    }
}
