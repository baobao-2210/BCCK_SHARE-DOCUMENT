package com.example.bcck.Profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bcck.R;
import com.example.bcck.HomeActivity;
// Đảm bảo import đúng file User của bạn
import com.example.bcck.Profile.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // Quan trọng để sửa lỗi NOT_FOUND

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    // Khai báo View theo đúng ID trong XML mới
    private ImageView btnBack, btnSettings, btnEditAvatar, imgAvatar;
    private TextView tvName, tvClass, tvDepartment, tvYear, tvEmail, tvTitle;
    private ConstraintLayout btnUpdateInfo, btnDocuments, btnPosts, btnLibrary, btnDownloadHistory, btnMyDocuments, btnSecurity;
    private ProgressBar progressBarAvatar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    // --- BỘ CHỌN ẢNH (Không cần xin quyền) ---
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Người dùng chọn ảnh xong -> Gọi hàm xử lý
                    xuLyVaLuuAnh(uri);
                } else {
                    Log.d("Profile", "Không chọn ảnh nào");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        // 2. Ánh xạ và cài đặt nút bấm
        initViews(view);
        setupButtons();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi màn hình hiện lên
        getUserProfile();
    }

    private void initViews(View view) {
        // Ánh xạ Header & Avatar
        btnBack = view.findViewById(R.id.btnBack);
        btnSettings = view.findViewById(R.id.btnSettings);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        progressBarAvatar = view.findViewById(R.id.progressBarAvatar); // Vòng tròn loading

        // Ánh xạ Thông tin User
        tvName = view.findViewById(R.id.tvName);
        tvClass = view.findViewById(R.id.tvClass);
        tvDepartment = view.findViewById(R.id.tvDepartment);
        tvYear = view.findViewById(R.id.tvYear);
        tvEmail = view.findViewById(R.id.tvEmail);

        // Ánh xạ Menu Card 1 (Tài liệu, Bài đăng...)
        btnDocuments = view.findViewById(R.id.btnDocuments);
        btnPosts = view.findViewById(R.id.btnPosts);
        btnLibrary = view.findViewById(R.id.btnLibrary);
        btnDownloadHistory = view.findViewById(R.id.btnDownloadHistory);
        btnMyDocuments = view.findViewById(R.id.btnMyDocuments);

        // Ánh xạ Menu Card 2 (Cập nhật, Bảo mật)
        btnUpdateInfo = view.findViewById(R.id.btnUpdateInfo);
        btnSecurity = view.findViewById(R.id.btnSecurity);
    }

    private void setupButtons() {
        // Nút Back -> Về Home
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // Nút Cập nhật thông tin -> Mở màn hình Edit
        btnUpdateInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Nút Camera -> Mở thư viện chọn ảnh
        btnEditAvatar.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Các nút khác (Demo Toast)
        btnDocuments.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.bcck.HomeActivity.class);
            startActivity(intent);
        });

        btnPosts.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.bcck.poster.MyPostsActivity.class);
            startActivity(intent);
        });

        btnLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.bcck.library.ThuVienActivity.class);
            startActivity(intent);
        });

        btnDownloadHistory.setOnClickListener(v ->
                Toast.makeText(getContext(), "Lịch sử tải xuống", Toast.LENGTH_SHORT).show());

        btnMyDocuments.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.bcck.poster.MyPostsActivity.class);
            startActivity(intent);
        });

        btnSecurity.setOnClickListener(v ->
                Toast.makeText(getContext(), "Bảo mật và quyền riêng tư", Toast.LENGTH_SHORT).show());

        btnSettings.setOnClickListener(v -> Toast.makeText(getContext(), "Cài đặt", Toast.LENGTH_SHORT).show());
    }

    // --- HÀM XỬ LÝ ẢNH BASE64 (MIỄN PHÍ - KHÔNG CẦN STORAGE) ---
    private void xuLyVaLuuAnh(Uri imageUri) {
        if (currentUserId == null) return;

        // Hiện loading
        progressBarAvatar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();

        try {
            // 1. Chuyển Uri thành Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

            // 2. Thu nhỏ ảnh (QUAN TRỌNG: Để không bị lỗi quá giới hạn 1MB của Firestore)
            // Giảm kích thước xuống tối đa 500px
            Bitmap scaledBitmap = scaleDown(bitmap, 500, true);

            // 3. Nén thành chuỗi String (Base64)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Chất lượng 70%
            byte[] imageBytes = baos.toByteArray();
            String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // 4. Lưu vào Firestore
            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", base64String);

            // Dùng SetOptions.merge() để: Nếu chưa có hồ sơ thì TẠO MỚI, có rồi thì CẬP NHẬT
            db.collection("users").document(currentUserId)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đổi ảnh thành công!", Toast.LENGTH_SHORT).show();

                        // Hiển thị ảnh mới ngay lập tức
                        Glide.with(this)
                                .load(imageBytes) // Glide load trực tiếp từ byte array
                                .circleCrop()
                                .into(imgAvatar);

                        progressBarAvatar.setVisibility(View.GONE); // Tắt loading
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBarAvatar.setVisibility(View.GONE);
                    });

        } catch (IOException e) {
            e.printStackTrace();
            progressBarAvatar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Lỗi đọc file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm phụ trợ để thu nhỏ ảnh (Giữ nguyên tỷ lệ khung hình)
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    // --- TẢI DỮ LIỆU TỪ FIREBASE ---
    private void getUserProfile() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUI(user);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Profile", "Lỗi tải dữ liệu", e));
    }

    private void updateUI(User user) {
        if (getContext() == null) return;

        // Set text
        // Kiểm tra null để tránh hiện "null" lên màn hình
        tvName.setText("Họ và tên : " + (user.getFullName() != null ? user.getFullName() : "..."));
        tvClass.setText("Lớp : " + (user.getClassName() != null ? user.getClassName() : "..."));
        tvDepartment.setText("Khoa : " + (user.getDepartment() != null ? user.getDepartment() : "..."));
        tvYear.setText("Khóa : " + (user.getCourseYear() != null ? user.getCourseYear() : "..."));
        tvEmail.setText("Email : " + (user.getEmail() != null ? user.getEmail() : "..."));

        // Xử lý hiển thị ảnh
        String avatarData = user.getAvatarUrl();
        if (avatarData != null && !avatarData.isEmpty()) {
            // Trường hợp 1: Nếu là link http (Cách cũ hoặc link mạng)
            if (avatarData.startsWith("http")) {
                Glide.with(this).load(avatarData).circleCrop().into(imgAvatar);
            }
            // Trường hợp 2: Nếu là chuỗi Base64 (Cách mới tiết kiệm tiền)
            else {
                try {
                    byte[] imageByteArray = Base64.decode(avatarData, Base64.DEFAULT);
                    Glide.with(this)
                            .load(imageByteArray)
                            .circleCrop()
                            .placeholder(R.drawable.ute)
                            .into(imgAvatar);
                } catch (IllegalArgumentException e) {
                    Log.e("Profile", "Lỗi giải mã ảnh Base64");
                }
            }
        }
    }
}
