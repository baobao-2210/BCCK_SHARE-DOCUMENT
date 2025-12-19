package com.example.bcck.Profile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.SetOptions;
import com.example.bcck.R;
// Nhớ import model User của bạn
import com.example.bcck.Profile.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtClass, edtDepartment, edtYear;
    private Button btnSave, btnCancel;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Ánh xạ
        edtFullName = findViewById(R.id.edtFullName);
        edtClass = findViewById(R.id.edtClass);
        edtDepartment = findViewById(R.id.edtDepartment);
        edtYear = findViewById(R.id.edtYear);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // 2. Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadOldData(); // Load dữ liệu cũ
        }

        // 3. Sự kiện nút Lưu
        btnSave.setOnClickListener(v -> saveToFirebase());

        // 4. Sự kiện nút Hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadOldData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            edtFullName.setText(user.getFullName());
                            edtClass.setText(user.getClassName());
                            edtDepartment.setText(user.getDepartment());
                            edtYear.setText(user.getCourseYear());
                        }
                    }
                });
    }

    private void saveToFirebase() {
        // Lấy dữ liệu từ ô nhập
        String strName = edtFullName.getText().toString().trim();
        String strClass = edtClass.getText().toString().trim();
        String strDept = edtDepartment.getText().toString().trim();
        String strYear = edtYear.getText().toString().trim();

        // Lấy email từ tài khoản đang đăng nhập (để lưu luôn nếu là lần đầu tạo)
        String strEmail = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            strEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }

        // Tạo Map dữ liệu
        Map<String, Object> data = new HashMap<>();
        data.put("fullName", strName);
        data.put("className", strClass);
        data.put("department", strDept);
        data.put("courseYear", strYear);
        data.put("email", strEmail); // Lưu thêm email để không bị trống

        // --- KHẮC PHỤC LỖI TẠI ĐÂY ---
        // Dùng .set() với SetOptions.merge() thay vì .update()
        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}