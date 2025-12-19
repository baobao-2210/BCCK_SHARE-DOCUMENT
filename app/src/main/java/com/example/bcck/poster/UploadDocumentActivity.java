package com.example.bcck.poster;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.bcck.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class UploadDocumentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialCardView uploadArea;
    private TextInputEditText etDocumentName, etSubject, etTeacher, etDescription;
    private Spinner spinnerCourse, spinnerYear;
    private Button btnUpload;

    private static final int PICK_FILE_REQUEST_CODE = 101;
    private Uri selectedFileUri = null;
    private String selectedFileName = "Chưa chọn tệp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        mapViews();
        setupSpinners();
        setupClickListeners();
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
        ArrayAdapter<String> courseAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courses);
        spinnerCourse.setAdapter(courseAdapter);

        String[] years = {"Chọn Năm học", "2023-2024", "2022-2023", "2021-2022", "2020-2021"};
        ArrayAdapter<String> yearAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        uploadArea.setOnClickListener(v -> openFilePicker());

        btnUpload.setOnClickListener(v -> {
            if (validateForm()) {
                uploadFileToFirebaseStorage();
            }
        });
    }

    // ================= FILE PICKER =================

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(
                Intent.createChooser(intent, "Chọn tệp tài liệu"),
                PICK_FILE_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            selectedFileName = getFileName(selectedFileUri);
            Toast.makeText(this, "Đã chọn: " + selectedFileName, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    // ================= VALIDATE =================

    private boolean validateForm() {
        if (etDocumentName.getText().toString().trim().isEmpty()
                || etSubject.getText().toString().trim().isEmpty()
                || etTeacher.getText().toString().trim().isEmpty()
                || spinnerCourse.getSelectedItemPosition() == 0
                || spinnerYear.getSelectedItemPosition() == 0
                || selectedFileUri == null) {

            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ================= FIREBASE STORAGE UPLOAD =================

    private void uploadFileToFirebaseStorage() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Bạn chưa chọn tệp!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";

        String safeName = sanitizeFileName(selectedFileName);
        String path = "documents/" + userId + "/" + System.currentTimeMillis() + "_" + safeName;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);

        Toast.makeText(this, "Đang tải lên file...", Toast.LENGTH_SHORT).show();

        ref.putFile(selectedFileUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri -> saveDocumentInfoToFirestore(uri.toString()))
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Lấy link file lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload Firebase thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String sanitizeFileName(String input) {
        String safe = (input == null || input.trim().isEmpty()) ? "document.pdf" : input.trim();
        safe = safe.replaceAll("[\\\\/:*?\"<>|\\n\\r\\t]", "_");
        safe = safe.replaceAll("\\s+", " ");
        if (!safe.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            safe += ".pdf";
        }
        return safe;
    }

    // ================= FIRESTORE =================

    private void saveDocumentInfoToFirestore(String downloadUrl) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : "unknown";

        String uploaderName = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getEmail()
                : "Ẩn danh";

        Document document = new Document();
        document.setTitle(etDocumentName.getText().toString());
        document.setDocType("PDF");
        document.setAuthorName(uploaderName);
        document.setSubject(etSubject.getText().toString());
        document.setTeacher(etTeacher.getText().toString());
        document.setMajor(spinnerCourse.getSelectedItem().toString());
        document.setYear(spinnerYear.getSelectedItem().toString());
        document.setDescription(etDescription.getText().toString());
        document.setFileUrl(downloadUrl);
        document.setUploaderId(userId);
        document.setUploaderName(uploaderName);
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
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lưu Firestore lỗi!", Toast.LENGTH_LONG).show()
                );
    }
}
