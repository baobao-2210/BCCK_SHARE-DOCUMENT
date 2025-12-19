package com.example.bcck.poster;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bcck.R;

public class MyPostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new com.example.bcck.poster.DocumentFragment())
                    .commit();
        }
    }
}