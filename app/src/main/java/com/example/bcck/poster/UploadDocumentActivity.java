package com.example.bcck.poster;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.bcck.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UploadDocumentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialCardView uploadArea;
    private TextInputEditText etDocumentName, etSubject, etTeacher, etDescription;
    private Spinner spinnerCourse, spinnerYear;
    private Button btnUpload;

    private static final int PICK_FILE_REQUEST_CODE = 101;
    private Uri selectedFileUri = null;
    private String selectedFileName = "Chưa chọn tệp";

    private static final String CLOUD_NAME = "djnddcxhq";
    private static final String UPLOAD_PRESET = "unsigned_preset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        initCloudinary();
        mapViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            MediaManager.init(this, config);
        } catch (Exception ignored) {
        }
    }

    private void mapViews() {
        btnBack = findViewById(R.id.btnBack);
        uploadArea = findViewById(R.id.uploadArea);
        etDocumentName = findViewById(R.id.etDocumentName);
        etSubject = findViewById(R.id.etSubject);
        etTeacher = findViewById(R.id.etTeacher);
        etDescription = findViewById(R.id.etDescription);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnUpload = findViewById(R.id.btnUpload);
    }

    private void setupSpinners() {
        String[] courses = {"Chọn Khoa", "CNTT", "Kỹ thuật Xây dựng", "Cơ Khí", "Hóa Học", "SPCN"};
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courses);
        spinnerCourse.setAdapter(courseAdapter);

        String[] years = {"Chọn Năm học", "2023-2024", "2022-2023", "2021-2022", "2020-2021"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        uploadArea.setOnClickListener(v -> openFilePicker());

        btnUpload.setOnClickListener(v -> {
            if (validateForm()) {
                uploadFileToCloudinary();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Chọn tài liệu"), PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Vui lòng cài đặt trình quản lý file.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri == null) return;

            selectedFileName = getFileName(selectedFileUri);

            if (etDocumentName.getText() != null && etDocumentName.getText().toString().trim().isEmpty()) {
                etDocumentName.setText(selectedFileName);
            }

            Toast.makeText(this, "Đã chọn: " + selectedFileName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri != null && "content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } catch (Exception ignored) {
            }
        }

        if (result == null && uri != null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result == null ? "file" : result;
    }

    private boolean validateForm() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etDocumentName.getText() == null || etDocumentName.getText().toString().trim().isEmpty()) {
            etDocumentName.setError("Nhập tên tài liệu");
            return false;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Chưa chọn file nào!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isPdf(Uri uri) {
        String type = null;
        try {
            type = getContentResolver().getType(uri);
        } catch (Exception ignored) {
        }

        return (type != null && type.contains("pdf")) ||
                (selectedFileName != null && selectedFileName.toLowerCase().endsWith(".pdf"));
    }

    private void uploadFileToCloudinary() {
        btnUpload.setEnabled(false);
        btnUpload.setText("Đang tải lên...");
        Toast.makeText(this, "Bắt đầu tải lên...", Toast.LENGTH_SHORT).show();

        String resourceType = "auto";
        if (selectedFileUri != null && isPdf(selectedFileUri)) resourceType = "raw";

        MediaManager.get().upload(selectedFileUri)
                .unsigned(UPLOAD_PRESET)
                .option("resource_type", resourceType)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String fileUrl = (String) resultData.get("secure_url");
                        Log.d("Upload", "Upload thành công: " + fileUrl);
                        runOnUiThread(() -> saveDocumentInfoToFirestore(fileUrl));
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            btnUpload.setEnabled(true);
                            btnUpload.setText("Tải lên");
                            Toast.makeText(UploadDocumentActivity.this,
                                    "Lỗi Upload: " + error.getDescription(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("Upload Error", error.getDescription());
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }

    private void saveDocumentInfoToFirestore(String downloadUrl) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            btnUpload.setEnabled(true);
            btnUpload.setText("Tải lên");
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail() != null ? auth.getCurrentUser().getEmail() : "Ẩn danh";

        // Lấy fullName từ users/{uid}
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String fullName = userDoc.getString("fullName");
                    if (fullName == null || fullName.trim().isEmpty()) fullName = email;

                    Document document = new Document();

                    document.setTitle(etDocumentName.getText() != null ? etDocumentName.getText().toString() : "");

                    String extension = "FILE";
                    if (selectedFileName != null && selectedFileName.contains(".")) {
                        extension = selectedFileName.substring(selectedFileName.lastIndexOf('.') + 1).toUpperCase();
                    }
                    document.setDocType(extension);

                    document.setAuthorName(fullName);
                    document.setSubject(etSubject.getText() != null ? etSubject.getText().toString() : "");
                    document.setTeacher(etTeacher.getText() != null ? etTeacher.getText().toString() : "");
                    document.setMajor(spinnerCourse.getSelectedItem() != null ? spinnerCourse.getSelectedItem().toString() : "");
                    document.setYear(spinnerYear.getSelectedItem() != null ? spinnerYear.getSelectedItem().toString() : "");
                    document.setDescription(etDescription.getText() != null ? etDescription.getText().toString() : "");

                    document.setFileUrl(downloadUrl);

                    document.setUploaderId(userId);
                    document.setUploaderName(email);        // email
                    document.setUploaderFullName(fullName); // tên hiển thị

                    document.setUploadTimestamp(System.currentTimeMillis());
                    document.setDownloads(0);
                    document.setLikes(0);
                    document.setRating(0);

                    db.collection("DocumentID")
                            .add(document)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Tải lên thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnUpload.setEnabled(true);
                                btnUpload.setText("Tải lên");
                                Toast.makeText(this, "Lỗi lưu Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnUpload.setEnabled(true);
                    btnUpload.setText("Tải lên");
                    Toast.makeText(this, "Không lấy được tên user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
