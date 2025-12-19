package com.example.bcck.poster;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DocumentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT = "DOCUMENT_DETAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_detail);

        Document document = (Document) getIntent().getSerializableExtra(EXTRA_DOCUMENT);
        if (document == null) {
            Toast.makeText(this, "Thiếu dữ liệu tài liệu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViewsAndData(document);
    }

    private void initViewsAndData(Document document) {
        TextView tvAuthorName = findViewById(R.id.tvAuthorName);
        TextView tvDocumentTitle = findViewById(R.id.tvDocumentTitle);
        TextView tvFileType = findViewById(R.id.tvFileType);
        TextView tvSubject = findViewById(R.id.tvSubject);
        TextView tvTeacher = findViewById(R.id.tvTeacher);
        TextView tvCourse = findViewById(R.id.tvCourse);
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvUploader = findViewById(R.id.tvUploader);
        TextView tvUploadDate = findViewById(R.id.tvUploadDate);
        TextView tvDownloads = findViewById(R.id.tvDownloads);
        TextView tvRating = findViewById(R.id.tvRating);

        Button btnDownload = findViewById(R.id.btnDownload);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnPreview = findViewById(R.id.btnPreview);
        Button btnMessage = findViewById(R.id.btnMessage);
        ImageView btnClose = findViewById(R.id.btnClose);

        tvAuthorName.setText(safe(document.getAuthorName()));
        tvDocumentTitle.setText(safe(document.getTitle()));
        tvFileType.setText(safe(document.getDocType()));
        tvSubject.setText(safe(document.getSubject()));
        tvTeacher.setText(safe(document.getTeacher()));
        tvCourse.setText(safe(document.getMajor()));
        tvYear.setText("Năm học: " + safe(document.getYear()));
        tvUploader.setText("Đăng bởi: " + safe(document.getUploaderName()));

        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(document.getUploadTimestamp()));
        tvUploadDate.setText("Ngày đăng: " + dateString);

        tvDownloads.setText(String.valueOf(document.getDownloads()));
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", document.getRating()));

        btnDownload.setOnClickListener(v -> handleDownload(document));
        btnShare.setOnClickListener(v -> handleShare(document));
        btnPreview.setOnClickListener(v -> handlePreview(document));
        btnMessage.setOnClickListener(v -> handleMessage(document));
        btnClose.setOnClickListener(v -> finish());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void handleDownload(Document document) {
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Link lỗi!", Toast.LENGTH_SHORT).show();
            return;
        }

        String downloadUrl = document.getFileUrl();
        if (downloadUrl.contains("/upload/")) {
            downloadUrl = downloadUrl.replace("/upload/", "/upload/fl_attachment/");
        }

        String fileName = document.getTitle() == null ? "tai_lieu" : document.getTitle();
        String extension = "pdf";

        if (document.getDocType() != null && !document.getDocType().isEmpty()) {
            extension = document.getDocType().toLowerCase();
            if (extension.equals("file") || extension.length() > 4) extension = "pdf";
        }

        if (!fileName.toLowerCase().endsWith("." + extension)) {
            fileName = fileName + "." + extension;
        } else {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) extension = fileName.substring(lastDot + 1).toLowerCase();
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle(fileName);
            request.setDescription("Đang tải tài liệu...");

            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType != null && !mimeType.isEmpty()) request.setMimeType(mimeType);

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(this, "Đang tải xuống: " + fileName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải xuống: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShare(Document document) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ tài liệu: " + safe(document.getTitle()));
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hãy xem tài liệu này: " + safe(document.getTitle()) + "\nLink tải: " + safe(document.getFileUrl()));
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ tài liệu qua..."));
    }

    private void handlePreview(Document document) {
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Không có file!", Toast.LENGTH_SHORT).show();
            return;
        }

        String googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=" + document.getFileUrl();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(googleViewerUrl));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(document.getFileUrl()));
            try {
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Không tìm thấy ứng dụng hỗ trợ xem file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleMessage(Document document) {

        String uploaderName = document.getUploaderName();
        String uploaderId = document.getUploaderId(); // Đây là ID người đăng

        if (uploaderId != null && !uploaderId.isEmpty()) {
            Intent intent = new Intent(this, ChatDetailActivity.class);

            // ĐỔI TÊN KEY Ở ĐÂY CHO KHỚP VỚI ChatDetailActivity
            // Lưu ý: Hiện tại ChatDetailActivity dùng chatId để lấy dữ liệu từ Firestore
            // Nếu bạn chưa có hệ thống tạo ChatId tự động, tạm thời ta gán uploaderId vào
            intent.putExtra("chatId", uploaderId);
            intent.putExtra("chatName", uploaderName);

            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ID người đăng!", Toast.LENGTH_SHORT).show();
            String receiverId = document.getUploaderId();

            // ưu tiên fullName, fallback email
            String receiverName = document.getUploaderFullName();
            if (receiverName == null || receiverName.trim().isEmpty()) {
                receiverName = document.getUploaderName();

            }

            if (receiverId == null || receiverId.trim().isEmpty()) {
                Toast.makeText(this, "Bài này thiếu uploaderId", Toast.LENGTH_SHORT).show();
                return;
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (receiverId.equals(myUid)) {
                Toast.makeText(this, "Bạn đang là người đăng bài", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("RECEIVER_ID", receiverId);
            intent.putExtra("RECEIVER_NAME", receiverName == null ? "Chat" : receiverName);
            startActivity(intent);
        }

    }
}
