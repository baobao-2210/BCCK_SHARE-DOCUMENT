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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
        // 1. Ánh xạ Views
        TextView tvAuthorName = findViewById(R.id.tvAuthorName);
        TextView tvDocumentTitle = findViewById(R.id.tvDocumentTitle); // Kiểm tra lại ID bên XML xem là tvDocumentTitle hay tvDocTitle nhé
        TextView tvFileType = findViewById(R.id.tvFileType);
        TextView tvSubject = findViewById(R.id.tvSubject);
        TextView tvTeacher = findViewById(R.id.tvTeacher);
        TextView tvCourse = findViewById(R.id.tvCourse); // Có thể bên XML bạn đặt là tvMajor
        TextView tvYear = findViewById(R.id.tvYear);
        TextView tvUploader = findViewById(R.id.tvUploader);
        TextView tvUploadDate = findViewById(R.id.tvUploadDate);

        // --- CÁC PHẦN THỐNG KÊ ---
        TextView tvDownloads = findViewById(R.id.tvDownloads);
        TextView tvRating = findViewById(R.id.tvRating);
        TextView tvLikes = findViewById(R.id.tvLikes); // <--- BẠN THIẾU DÒNG NÀY

        Button btnDownload = findViewById(R.id.btnDownload);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnPreview = findViewById(R.id.btnPreview);
        Button btnMessage = findViewById(R.id.btnMessage);
        ImageView btnClose = findViewById(R.id.btnClose);

        // 2. Đổ dữ liệu
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

        // --- HIỂN THỊ SỐ LIỆU ---
        tvDownloads.setText(String.valueOf(document.getDownloads()));
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", document.getRating()));

        // <--- BẠN THIẾU DÒNG NÀY:
        tvLikes.setText(String.valueOf(document.getLikes()));

        // 3. Sự kiện Click
        btnDownload.setOnClickListener(v -> handleDownload(document));
        btnShare.setOnClickListener(v -> handleShare(document));
        btnPreview.setOnClickListener(v -> handlePreview(document));
        btnMessage.setOnClickListener(v -> handleMessage(document));
        btnClose.setOnClickListener(v -> finish());

        if (tvLikes != null) {
            tvLikes.setOnClickListener(v -> handleLike(document));
        }

        // 2. Bấm vào Rating -> Hiện bảng đánh giá
        if (tvRating != null) {
            tvRating.setOnClickListener(v -> showRatingDialog(document));
        }
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
                updateDownloadCount(document);
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
        String receiverId = document.getUploaderId();
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

        String receiverName = document.getUploaderFullName();
        if (receiverName == null || receiverName.trim().isEmpty()) receiverName = document.getUploaderName();
        if (receiverName == null || receiverName.trim().isEmpty()) receiverName = "Người đăng";

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("RECEIVER_NAME", receiverName);
        startActivity(intent);
    }
    private void updateDownloadCount(Document document) {
        if (document.getDocId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Gửi lệnh lên Firestore: Tăng field "downloads" thêm 1
        db.collection("DocumentID").document(document.getDocId())
                .update("downloads", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    // 2. Nếu thành công, cập nhật giao diện ngay lập tức
                    int newCount = document.getDownloads() + 1;
                    document.setDownloads(newCount); // Cập nhật biến cục bộ

                    TextView tvDownloads = findViewById(R.id.tvDownloads);
                    tvDownloads.setText(String.valueOf(newCount));

                    // (Tùy chọn) Toast báo thành công
                    // Toast.makeText(this, "Đã cập nhật lượt tải!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void handleLike(Document document) {
        if (document.getDocId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Gửi lệnh tăng 1 like lên Server
        db.collection("DocumentID").document(document.getDocId())
                .update("likes", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật giao diện ngay lập tức
                    int newLike = document.getLikes() + 1;
                    document.setLikes(newLike);

                    TextView tvLikes = findViewById(R.id.tvLikes);
                    if (tvLikes != null) {
                        tvLikes.setText(String.valueOf(newLike));
                    }
                    Toast.makeText(this, "Đã thích tài liệu!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void showRatingDialog(Document document) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đánh giá tài liệu");

        // Tạo Layout chứa thanh RatingBar
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        android.widget.RatingBar ratingBar = new android.widget.RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1);
        ratingBar.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        // Set layout gravity center cho đẹp
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) ratingBar.getLayoutParams();
        params.gravity = android.view.Gravity.CENTER;
        ratingBar.setLayoutParams(params);

        layout.addView(ratingBar);
        builder.setView(layout);

        builder.setPositiveButton("Gửi đánh giá", (dialog, which) -> {
            float userRating = ratingBar.getRating();
            if (userRating > 0) {
                updateRating(document, userRating);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void updateRating(Document document, float newRatingByUser) {
        if (document.getDocId() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Tính toán điểm trung bình mới (Local)
        float currentRating = document.getRating();
        int currentCount = document.getRatingCount();

        // Công thức: (Điểm cũ * số lượt cũ + Điểm mới) / (Số lượt cũ + 1)
        float newAverage = ((currentRating * currentCount) + newRatingByUser) / (currentCount + 1);
        int newCount = currentCount + 1;

        // 2. Cập nhật lên Firestore
        db.collection("DocumentID").document(document.getDocId())
                .update(
                        "rating", newAverage,
                        "ratingCount", newCount
                )
                .addOnSuccessListener(aVoid -> {
                    // 3. Cập nhật giao diện
                    document.setRating(newAverage);
                    document.setRatingCount(newCount);

                    TextView tvRating = findViewById(R.id.tvRating);
                    if (tvRating != null) {
                        tvRating.setText(String.format(Locale.getDefault(), "%.1f", newAverage));
                    }
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
