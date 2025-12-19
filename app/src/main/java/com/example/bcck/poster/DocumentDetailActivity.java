package com.example.bcck.poster;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;

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

        if (document != null) {
            initViewsAndData(document);
        } else {
            finish();
        }
    }

    private void initViewsAndData(Document document) {
        // 1. THAM CHIẾU VIEWS
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

        // 2. GÁN DỮ LIỆU
        tvAuthorName.setText(document.getAuthorName());
        tvDocumentTitle.setText(document.getTitle());
        tvFileType.setText(document.getDocType());
        tvSubject.setText(document.getSubject());
        tvTeacher.setText(document.getTeacher());
        tvCourse.setText(document.getMajor());
        tvYear.setText("Năm học: " + document.getYear());
        tvUploader.setText("Đăng bởi: " + document.getUploaderName());

        String dateString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(document.getUploadTimestamp()));
        tvUploadDate.setText("Ngày đăng: " + dateString);

        tvDownloads.setText(String.valueOf(document.getDownloads()));
        tvRating.setText(String.format("%.1f", document.getRating()));

        // 3. SỰ KIỆN CLICK
        btnDownload.setOnClickListener(v -> handleDownload(document));
        btnShare.setOnClickListener(v -> handleShare(document));
        btnPreview.setOnClickListener(v -> handlePreview(document));
        btnMessage.setOnClickListener(v -> handleMessage(document));
        btnClose.setOnClickListener(v -> finish());
    }

    // ================== XỬ LÝ TẢI XUỐNG (QUAN TRỌNG) ==================
    // ================== CODE SỬA LỖI ĐUÔI FILE ==================
    private void handleDownload(Document document) {
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Link lỗi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Xử lý Link (Thêm fl_attachment)
        String downloadUrl = document.getFileUrl();
        if (downloadUrl.contains("/upload/")) {
            downloadUrl = downloadUrl.replace("/upload/", "/upload/fl_attachment/");
        }

        // 2. Xử lý Tên file và Đuôi file
        String fileName = document.getTitle();
        String extension = "pdf"; // Mặc định

        // Lấy đuôi file chuẩn từ Firestore (nếu có)
        if (document.getDocType() != null && !document.getDocType().isEmpty()) {
            extension = document.getDocType().toLowerCase();
            if (extension.equals("file") || extension.length() > 4) extension = "pdf";
        }

        // Nếu tên file chưa có đuôi, tự động nối thêm
        if (!fileName.toLowerCase().endsWith("." + extension)) {
            fileName = fileName + "." + extension;
        } else {
            // Trường hợp tên đã có đuôi (VD: BaoCao.docx), ta cần tách cái đuôi "docx" ra để dùng bên dưới
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle(fileName);
            request.setDescription("Đang tải tài liệu...");

            // ==================================================================
            // PHẦN MỚI QUAN TRỌNG: TỰ ĐỘNG XÁC ĐỊNH MIME TYPE
            // ==================================================================
            // Dựa vào đuôi file (docx, pdf, xlsx...), ta nhờ Android tìm MimeType chuẩn
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            // Nếu tìm thấy MimeType chuẩn (ví dụ application/msword), gán vào request
            if (mimeType != null && !mimeType.isEmpty()) {
                request.setMimeType(mimeType);
            }
            // ==================================================================

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

    // ================== CÁC CHỨC NĂNG KHÁC ==================

    private void handleShare(Document document) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ tài liệu: " + document.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hãy xem tài liệu này: " + document.getTitle() +
                "\nLink tải: " + document.getFileUrl());
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ tài liệu qua..."));
    }

    private void handlePreview(Document document) {
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Không có file!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cách 1: Thử dùng Google Docs Viewer
        String googleViewerUrl = "https://docs.google.com/gview?embedded=true&url=" + document.getFileUrl();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(googleViewerUrl));

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Cách 2: Nếu Google Viewer lỗi, mở trực tiếp file gốc để trình duyệt tự xử lý
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(document.getFileUrl()));
            try {
                startActivity(browserIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Không tìm thấy ứng dụng hỗ trợ xem file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleMessage(Document document) {
        String receiverName = document.getUploaderName();
        String receiverId = document.getUploaderId();

        if (receiverName != null && !receiverName.isEmpty() && receiverId != null && !receiverId.isEmpty()) {
            Intent intent = new Intent(this, ChatDetailActivity.class);
            intent.putExtra("RECEIVER_NAME", receiverName);
            intent.putExtra("RECEIVER_ID", receiverId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không thể xác định người nhận để chat.", Toast.LENGTH_SHORT).show();
        }
    }
}