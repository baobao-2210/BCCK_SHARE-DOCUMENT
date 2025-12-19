package com.example.bcck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private AppCompatButton btnSinhVien, btnGiangVien;
    private EditText edtGmail, edtMatKhau;
    private MaterialButton btnSubmitDangNhap;

    private boolean isSinhVien = true;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();  // 🔥 khởi tạo Firebase

        initViews();
        setupInitialState();
        setupListeners();
        updateRoleToggle();
    }

    private void initViews() {
        btnSinhVien = findViewById(R.id.btnSinhVien);
        btnGiangVien = findViewById(R.id.btnGiangVien);

        edtGmail = findViewById(R.id.edtGmail);
        edtMatKhau = findViewById(R.id.edtMatKhau);

        btnSubmitDangNhap = findViewById(R.id.btnSubmitDangNhap);
    }

    private void setupInitialState() {
        btnSubmitDangNhap.setText("Đăng Nhập");
    }

    private void setupListeners() {
        btnSinhVien.setOnClickListener(v -> {
            isSinhVien = true;
            updateRoleToggle();
        });

        btnGiangVien.setOnClickListener(v -> {
            isSinhVien = false;
            updateRoleToggle();
        });

        btnSubmitDangNhap.setOnClickListener(v -> handleLogin());
    }

    private void updateRoleToggle() {
        if (isSinhVien) {
            btnSinhVien.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnSinhVien.setTextColor(Color.WHITE);
            btnGiangVien.setBackgroundColor(Color.TRANSPARENT);
            btnGiangVien.setTextColor(Color.parseColor("#090909"));
        } else {
            btnGiangVien.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnGiangVien.setTextColor(Color.WHITE);
            btnSinhVien.setBackgroundColor(Color.TRANSPARENT);
            btnSinhVien.setTextColor(Color.parseColor("#090909"));
        }
    }

    private void handleLogin() {
        String gmail = edtGmail.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();

        if (gmail.isEmpty() || matKhau.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        btnSubmitDangNhap.setEnabled(false);
        btnSubmitDangNhap.setText("Đang xử lý...");

        // 🔥 FIREBASE LOGIN
        mAuth.signInWithEmailAndPassword(gmail, matKhau)
                .addOnCompleteListener(task -> {
                    btnSubmitDangNhap.setEnabled(true);
                    btnSubmitDangNhap.setText("Đăng Nhập");

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // CHUYỂN SANG HOME
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
