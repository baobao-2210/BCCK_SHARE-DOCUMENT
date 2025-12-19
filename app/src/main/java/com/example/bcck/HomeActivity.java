package com.example.bcck;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.bcck.Chat.ChatFragment;
import com.example.bcck.Home.HomeFragment;
import com.example.bcck.Profile.ProfileFragment;
import com.example.bcck.group.GroupFragment;
import com.example.bcck.poster.DocumentFragment;

public class HomeActivity extends AppCompatActivity {

    // Bottom Navigation Views
    private ConstraintLayout navDocs, navGroup, navLibrary, navChat, navProfile;
    private ImageView navDocsIcon, navGroupIcon, navLibraryIcon, navChatIcon, navProfileIcon;
    private TextView navDocsText, navGroupText, navLibraryText, navChatText, navProfileText;

    private int currentFragmentIndex = 0; // Theo dõi fragment hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Khởi tạo views
        initViews();

        // Setup click listeners cho Bottom Navigation
        setupBottomNavigation();

        // **CHỈNH SỬA TẠI ĐÂY:**
        if (savedInstanceState == null) {
            // 1. Gọi hàm loadFragment để thực hiện Fragment Transaction
            loadFragment(new HomeFragment(), 0);

            // 2. CẬP NHẬT GIAO DIỆN Bottom Nav (QUAN TRỌNG)
            // Lệnh này đảm bảo icon Trang Chủ được tô sáng ngay từ đầu
            updateBottomNav(0);
        }
    }

    private void initViews() {
        // Tìm các ConstraintLayout của Bottom Navigation
        navDocs = findViewById(R.id.navDocs);
        navGroup = findViewById(R.id.navGroup);
        navLibrary = findViewById(R.id.navLibrary);
        navChat = findViewById(R.id.navChat);
        navProfile = findViewById(R.id.navProfile);

        // Tìm các ImageView icons
        navDocsIcon = findViewById(R.id.navDocsIcon);
        navGroupIcon = findViewById(R.id.navGroupIcon);
        navLibraryIcon = findViewById(R.id.navLibraryIcon);
        navChatIcon = findViewById(R.id.navChatIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);

        // Tìm các TextView labels
        navDocsText = findViewById(R.id.navDocsText);
        navGroupText = findViewById(R.id.navGroupText);
        navLibraryText = findViewById(R.id.navLibraryText);
        navChatText = findViewById(R.id.navChatText);
        navProfileText = findViewById(R.id.navProfileText);
    }
    private void setupBottomNavigation() {
        // Tab Trang Chủ
        navDocs.setOnClickListener(v -> {
            loadFragment(new HomeFragment(), 0);
            updateBottomNav(0);
        });

        // Tab Nhóm
        navGroup.setOnClickListener(v -> {
            loadFragment(new GroupFragment(), 1);
            updateBottomNav(1);
        });

        // Tab Tài Liệu
        navLibrary.setOnClickListener(v -> {
            loadFragment(new DocumentFragment(), 2);
            updateBottomNav(2);
        });

        // Tab Chat
        navChat.setOnClickListener(v -> {
            loadFragment(new ChatFragment(), 3);
            updateBottomNav(3);
        });

        // Tab Cá nhân
        navProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), 4);
            updateBottomNav(4);
        });
    }

    private void loadFragment(Fragment fragment, int index) {

        // Nếu fragment chưa từng được thêm, vẫn cho phép load
        if (currentFragmentIndex == index &&
                getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null) {
            return;
        }

        currentFragmentIndex = index;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void updateBottomNav(int selectedIndex) {
        // Reset tất cả về màu xám
        resetNavItem(navDocsIcon, navDocsText);
        resetNavItem(navGroupIcon, navGroupText);
        resetNavItem(navLibraryIcon, navLibraryText);
        resetNavItem(navChatIcon, navChatText);
        resetNavItem(navProfileIcon, navProfileText);

        // Highlight item được chọn
        switch (selectedIndex) {
            case 0:
                highlightNavItem(navDocsIcon, navDocsText);
                break;
            case 1:
                highlightNavItem(navGroupIcon, navGroupText);
                break;
            case 2:
                highlightNavItem(navLibraryIcon, navLibraryText);
                break;
            case 3:
                highlightNavItem(navChatIcon, navChatText);
                break;
            case 4:
                highlightNavItem(navProfileIcon, navProfileText);
                break;
        }
    }

    private void resetNavItem(ImageView icon, TextView text) {
        // Màu xám cho item không được chọn
        icon.setColorFilter(0xFF666666);
        text.setTextColor(0xFF666666);
    }

    private void highlightNavItem(ImageView icon, TextView text) {
        // Màu tím cho item được chọn
        icon.setColorFilter(0xFF5B5FC7);
        text.setTextColor(0xFF5B5FC7);
    }
}