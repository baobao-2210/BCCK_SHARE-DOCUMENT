package com.example.bcck.Profile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bcck.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Trỏ vào file xml vừa tạo ở Bước 1

        // Kiểm tra để tránh tạo lại Fragment khi xoay màn hình
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_profile, new ProfileFragment())
                    .commit();
        }
    }
}