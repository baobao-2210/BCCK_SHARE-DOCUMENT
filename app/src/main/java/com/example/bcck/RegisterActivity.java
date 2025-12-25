package com.example.bcck;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final long LOGIN_TIMEOUT_MS = 12000L;

    private AppCompatButton btnSinhVien, btnGiangVien;
    private EditText edtGmail, edtMatKhau;
    private MaterialButton btnSubmitDangNhap;

    private boolean isSinhVien = true;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
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
        String email = edtGmail.getText().toString().trim();
        String password = edtMatKhau.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        startTimeout();

        Log.d(TAG, "signInWithEmailAndPassword: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        finishLoading();

                        String msg = (task.getException() != null && task.getException().getMessage() != null)
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại";

                        Log.e(TAG, "Login failed: " + msg, task.getException());

                        if (msg.contains("no user record")) {
                            Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                        } else if (msg.toLowerCase().contains("password")) {
                            Toast.makeText(this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    FirebaseUser user = (task.getResult() != null) ? task.getResult().getUser() : mAuth.getCurrentUser();
                    if (user == null) {
                        finishLoading();
                        Toast.makeText(this, "Không lấy được thông tin user", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();
                    Log.d(TAG, "Login success, uid=" + uid);

                    // Lấy user profile trên Firestore
                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnCompleteListener(userTask -> {
                                if (!userTask.isSuccessful()) {
                                    finishLoading();
                                    Log.e(TAG, "Get user doc failed", userTask.getException());
                                    Toast.makeText(this, "Lỗi dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    return;
                                }

                                var document = userTask.getResult();
                                if (document == null) {
                                    finishLoading();
                                    Toast.makeText(this, "Không đọc được dữ liệu user", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    return;
                                }

                                // CHƯA CÓ USER → TẠO
                                if (!document.exists()) {
                                    Log.d(TAG, "User doc not exist -> creating...");

                                    Map<String, Object> newUser = new HashMap<>();
                                    newUser.put("email", email);
                                    newUser.put("role", isSinhVien ? "student" : "teacher");
                                    newUser.put("isActive", true);
                                    newUser.put("createdAt", FieldValue.serverTimestamp());

                                    db.collection("users")
                                            .document(uid)
                                            .set(newUser)
                                            .addOnCompleteListener(createTask -> {
                                                if (!createTask.isSuccessful()) {
                                                    finishLoading();
                                                    Log.e(TAG, "Create user doc failed", createTask.getException());
                                                    Toast.makeText(this, "Không tạo được user", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();
                                                    return;
                                                }

                                                finishLoading();
                                                goHome();
                                            });

                                    return;
                                }

                                // CHECK KHÓA
                                Boolean isActive = document.getBoolean("isActive");
                                if (isActive != null && !isActive) {
                                    finishLoading();
                                    Toast.makeText(this, "Tài khoản đã bị khóa", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    return;
                                }

                                finishLoading();
                                goHome();
                            });
                });
    }

    private void goHome() {
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        btnSubmitDangNhap.setEnabled(!loading);
        btnSubmitDangNhap.setText(loading ? "Đang xử lý..." : "Đăng Nhập");
    }

    private void finishLoading() {
        stopTimeout();
        setLoading(false);
    }

    private void startTimeout() {
        stopTimeout();
        timeoutRunnable = () -> {
            // Nếu tới đây mà vẫn chưa xong -> trả UI về bình thường
            Log.e(TAG, "Login timeout!");
            finishLoading();
            Toast.makeText(this, "Hết thời gian chờ. Kiểm tra mạng/Firebase", Toast.LENGTH_SHORT).show();
        };
        handler.postDelayed(timeoutRunnable, LOGIN_TIMEOUT_MS);
    }

    private void stopTimeout() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
