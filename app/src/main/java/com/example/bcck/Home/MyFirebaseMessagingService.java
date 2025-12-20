package com.example.bcck.Home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "msg_channel";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        saveToken(token);
    }

    private void saveToken(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(data, SetOptions.merge());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String chatId = remoteMessage.getData().get("chatId");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (title == null) title = "Tin nhắn mới";
        if (body == null) body = "Bạn có tin nhắn mới";

        showNoti(title, body, chatId);
    }

    private void showNoti(String title, String body, String chatId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ChatDetailActivity.class);
        if (chatId != null && !chatId.isEmpty()) intent.putExtra("chatId", chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
