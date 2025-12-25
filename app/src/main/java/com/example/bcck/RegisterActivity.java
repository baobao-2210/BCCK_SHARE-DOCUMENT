package com.example.bcck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtGmail, edtMatKhau;
    private MaterialButton btnLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtGmail = findViewById(R.id.edtGmail);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        btnLogin = findViewById(R.id.btnSubmitDangNhap);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String email = edtGmail.getText().toString().trim();
        String pass = edtMatKhau.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

        timeoutRunnable = () -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
            Toast.makeText(this, "Hết thời gian chờ", Toast.LENGTH_SHORT).show();
        };
        handler.postDelayed(timeoutRunnable, 12000);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(auth -> {
                    String uid = auth.getUser().getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {

                                if (!doc.exists()) {
                                    // 🔥 TẠO USER FIRESTORE LẦN ĐẦU
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("email", email);
                                    user.put("role", "user");
                                    user.put("isActive", true);

                                    db.collection("users").document(uid)
                                            .set(user)
                                            .addOnSuccessListener(v -> goHome());
                                } else {
                                    Boolean isActive = doc.getBoolean("isActive");
                                    if (isActive != null && !isActive) {
                                        Toast.makeText(this, "Tài khoản bị khóa", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();
                                        resetUI();
                                        return;
                                    }
                                    goHome();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    resetUI();
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                });
    }

    private void goHome() {
        handler.removeCallbacks(timeoutRunnable);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void resetUI() {
        handler.removeCallbacks(timeoutRunnable);
        btnLogin.setEnabled(true);
        btnLogin.setText("Đăng nhập");
    }
}
