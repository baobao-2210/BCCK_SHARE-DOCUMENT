package com.example.bcck.poster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.example.bcck.Chat.ChatDetailActivity;
import com.example.bcck.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DocumentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT = "DOCUMENT_DETAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_detail);

        Document document = (Document) getIntent().getSerializableExtra(EXTRA_DOCUMENT);

        if (document != null) {

            // 1. THAM CHIẾU TẤT CẢ CÁC VIEWS (KHAI BÁO MỘT LẦN)
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

            // Tham chiếu các nút thao tác
            Button btnDownload = findViewById(R.id.btnDownload);
            Button btnShare = findViewById(R.id.btnShare);
            Button btnPreview = findViewById(R.id.btnPreview);
            Button btnMessage = findViewById(R.id.btnMessage);
            // KHAI BÁO btnClose LẦN DUY NHẤT Ở ĐÂY
            ImageView btnClose = findViewById(R.id.btnClose);

            // 2. GÁN DỮ LIỆU HIỂN THỊ
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


            // 3. GÁN ONCLICK LISTENER (Các nút thao tác)

            // Nút Tải xuống
            btnDownload.setOnClickListener(v -> {
                handleDownload(document);
            });

            // Nút Chia sẻ
            btnShare.setOnClickListener(v -> {
                handleShare(document);
            });

            // Nút Xem trước
            btnPreview.setOnClickListener(v -> {
                handlePreview(document);
            });

            // Nút Nhắn tin
            btnMessage.setOnClickListener(v -> {
                handleMessage(document);
            });

            // Nút Thoát/Đóng (btnClose)
            btnClose.setOnClickListener(v -> {
                finish(); // <-- Thao tác đóng Activity
            });

        } else {
            finish();
        }
    }


    private void handleDownload(Document document) {

        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "File không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = buildSafeFileName(document.getTitle());
        downloadFromFirebaseToAppDownloads(document.getFileUrl(), fileName, true);
    }


    private void handleShare(Document document) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ tài liệu: " + document.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hãy xem tài liệu này: " + document.getTitle() +
                "\nLink tải (nếu có): https://docs.google.com/document/create?hl=vi");
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ tài liệu qua..."));
    }

    private void handlePreview(Document document) {

        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            Toast.makeText(this, "Không có file!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = buildSafeFileName(document.getTitle());
        downloadFromFirebaseToCacheAndPreview(document.getFileUrl(), fileName);
    }



    private void handleMessage(Document document) {
        // Lấy thông tin cần thiết từ Document
        String receiverName = document.getUploaderName();
        // Giả sử có hàm getUploaderId() trong lớp Document
        String receiverId = document.getUploaderId();
        // Kiểm tra dữ liệu và khởi chạy Activity
        if (receiverName != null && !receiverName.isEmpty() && receiverId != null && !receiverId.isEmpty()) {

            // 1. Tạo Intent
            // CHÚ Ý: Đảm bảo đã import com.example.bcck.chat.ChatActivity;
            Intent intent = new Intent(this, ChatDetailActivity.class);

            // 2. Truyền dữ liệu người nhận.
            // SỬ DỤNG CÁC KEY MÀ ChatActivity CỦA BẠN DÙNG ĐỂ NHẬN DỮ LIỆU.
            intent.putExtra("RECEIVER_NAME", receiverName);
            intent.putExtra("RECEIVER_ID", receiverId);

            // 3. Khởi chạy
            startActivity(intent);

        } else {
            Toast.makeText(this, "Không thể xác định người nhận để chat.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildSafeFileName(String title) {
        String safe = (title == null || title.trim().isEmpty()) ? "document" : title.trim();
        safe = safe.replaceAll("[\\\\/:*?\"<>|\\n\\r\\t]", "_");
        safe = safe.replaceAll("\\s+", " ");
        if (!safe.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            safe += ".pdf";
        }
        if (safe.length() > 80) {
            safe = safe.substring(0, 80);
            if (!safe.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                safe += ".pdf";
            }
        }
        return safe;
    }

    private void downloadFromFirebaseToCacheAndPreview(String fileUrl, String fileName) {
        try {
            File previewDir = new File(getCacheDir(), "pdf_previews");
            if (!previewDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                previewDir.mkdirs();
            }

            File outFile = new File(previewDir, fileName);
            if (outFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outFile.delete();
            }

            Toast.makeText(this, "Đang tải file để xem trước...", Toast.LENGTH_SHORT).show();

            downloadToFile(fileUrl, outFile, new DownloadToFileCallback() {
                @Override
                public void onSuccess(File file) {
                    Intent intent = new Intent(DocumentDetailActivity.this, PdfPreviewActivity.class);
                    intent.putExtra(PdfPreviewActivity.EXTRA_FILE_PATH, file.getAbsolutePath());
                    startActivity(intent);
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(DocumentDetailActivity.this, "Không tải được file: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Không thể xem trước: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void downloadFromFirebaseToAppDownloads(String fileUrl, String fileName, boolean openAfterDownload) {
        try {
            File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir == null) {
                Toast.makeText(this, "Không truy cập được thư mục tải xuống.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!downloadsDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                downloadsDir.mkdirs();
            }

            File outFile = new File(downloadsDir, fileName);
            if (outFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outFile.delete();
            }

            Toast.makeText(this, "Đang tải xuống...", Toast.LENGTH_SHORT).show();

            downloadToFile(fileUrl, outFile, new DownloadToFileCallback() {
                @Override
                public void onSuccess(File file) {
                    Toast.makeText(DocumentDetailActivity.this, "Đã tải: " + file.getName(), Toast.LENGTH_SHORT).show();
                    if (openAfterDownload) {
                        Intent intent = new Intent(DocumentDetailActivity.this, PdfPreviewActivity.class);
                        intent.putExtra(PdfPreviewActivity.EXTRA_FILE_PATH, file.getAbsolutePath());
                        startActivity(intent);
                    }
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(DocumentDetailActivity.this, "Tải xuống thất bại: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Tải xuống thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private interface DownloadToFileCallback {
        void onSuccess(File file);

        void onError(Exception error);
    }

    private void downloadToFile(String fileUrl, File outFile, DownloadToFileCallback callback) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("URL rỗng"));
            return;
        }

        if (isFirebaseStorageUrl(fileUrl)) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
            ref.getFile(outFile)
                    .addOnSuccessListener(taskSnapshot -> callback.onSuccess(outFile))
                    .addOnFailureListener(e -> callback.onError(new Exception(e)));
            return;
        }

        downloadHttpToFile(fileUrl, outFile, callback);
    }

    private boolean isFirebaseStorageUrl(String url) {
        return url.startsWith("gs://")
                || url.startsWith("https://firebasestorage.googleapis.com/")
                || url.contains(".appspot.com/");
    }

    private void downloadHttpToFile(String url, File outFile, DownloadToFileCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> callback.onError(e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    String host;
                    try {
                        host = Uri.parse(url).getHost();
                    } catch (Exception ignored) {
                        host = null;
                    }
                    String message = "HTTP " + response.code() + (host != null ? (" (" + host + ")") : "");
                    runOnUiThread(() -> callback.onError(new Exception(message)));
                    return;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    runOnUiThread(() -> callback.onError(new Exception("Empty body")));
                    return;
                }

                try (InputStream in = body.byteStream(); FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                    runOnUiThread(() -> callback.onSuccess(outFile));
                } catch (Exception e) {
                    runOnUiThread(() -> callback.onError(e));
                } finally {
                    response.close();
                }
            }
        });
    }
}
