package com.example.bcck;

import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final long LOGIN_TIMEOUT_MS = 12000L;
    private AppCompatButton btnSinhVien, btnGiangVien;
    private EditText edtGmail, edtMatKhau;
    private MaterialButton btnSubmitDangNhap;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable loginTimeoutRunnable;

    private boolean isSinhVien = true;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

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

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối mạng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        scheduleLoginTimeout();

        // 🔥 Thực hiện đăng nhập Firebase
        mAuth.signInWithEmailAndPassword(gmail, matKhau)
                .addOnCompleteListener(this, task -> {
                    cancelLoginTimeout();
                    setLoadingState(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(RegisterActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        
                        // Thông báo lỗi cụ thể để bạn dễ debug
                        if (errorMessage.contains("no user record")) {
                            Toast.makeText(this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                        } else if (errorMessage.contains("password is invalid")) {
                            Toast.makeText(this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void scheduleLoginTimeout() {
        cancelLoginTimeout();
        loginTimeoutRunnable = () -> {
            setLoadingState(false);
            Toast.makeText(this, "Hết thời gian chờ. Vui lòng kiểm tra mạng.", Toast.LENGTH_SHORT).show();
        };
        mainHandler.postDelayed(loginTimeoutRunnable, LOGIN_TIMEOUT_MS);
    }

    private void cancelLoginTimeout() {
        if (loginTimeoutRunnable != null) {
            mainHandler.removeCallbacks(loginTimeoutRunnable);
            loginTimeoutRunnable = null;
        }
    }

    private void setLoadingState(boolean isLoading) {
        btnSubmitDangNhap.setEnabled(!isLoading);
        btnSubmitDangNhap.setText(isLoading ? "Đang xử lý..." : "Đăng Nhập");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null
                && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
