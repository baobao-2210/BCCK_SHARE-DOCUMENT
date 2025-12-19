package com.example.bcck.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ThuVienActivity extends AppCompatActivity implements CollectionAdapter.OnCollectionListener {

    private RecyclerView recyclerView;
    private CollectionAdapter adapter;
    private List<Collection> collections;
    private TabLayout tabLayout;
    private MaterialButton btnCreateCollection;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitythuvien);

        // Khởi tạo views
        initViews();

        // ⚡ THÊM TOOLBAR + NÚT QUAY LẠI TẠI ĐÂY
        setupToolbar();

        // Khởi tạo dữ liệu mẫu
        initData();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tabLayout = findViewById(R.id.tabLayout);
        btnCreateCollection = findViewById(R.id.btnCreateCollection);
        toolbar = findViewById(R.id.toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Bộ sưu tập"));
        tabLayout.addTab(tabLayout.newTab().setText("Yêu thích"));
    }

    // ⭐ Thêm Toolbar + Back Button
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // Trở về màn trước
        });
    }

    private void initData() {
        collections = new ArrayList<>();
        collections.add(new Collection("Ôn thi giữa kỳ", 12, "#5B8DEE"));
        collections.add(new Collection("Đồ án môn học", 8, "#A855F7"));
        collections.add(new Collection("Tài liệu tham khảo", 25, "#22C55E"));
    }

    private void setupRecyclerView() {
        adapter = new CollectionAdapter(collections, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnCreateCollection.setOnClickListener(v -> showCreateDialog());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.updateData(collections);
                } else {
                    Toast.makeText(ThuVienActivity.this, "Chức năng Yêu thích", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_collection, null);
        EditText etName = dialogView.findViewById(R.id.etCollectionName);

        builder.setView(dialogView)
                .setTitle("Tạo bộ sưu tập mới")
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String[] colors = {"#5B8DEE", "#A855F7", "#22C55E", "#EF4444", "#F59E0B"};
                        String randomColor = colors[(int) (Math.random() * colors.length)];
                        Collection newCollection = new Collection(name, 0, randomColor);
                        collections.add(newCollection);
                        adapter.notifyItemInserted(collections.size() - 1);
                        Toast.makeText(this, "Đã tạo bộ sưu tập: " + name, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onEditClick(int position) {
        Collection collection = collections.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_collection, null);

        EditText etName = dialogView.findViewById(R.id.etCollectionName);
        etName.setText(collection.getName());

        builder.setView(dialogView)
                .setTitle("Sửa bộ sưu tập")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        collection.setName(newName);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Đã cập nhật: " + newName, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDeleteClick(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bộ sưu tập")
                .setMessage("Bạn có chắc muốn xóa bộ sưu tập này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String name = collections.get(position).getName();
                    collections.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Đã xóa: " + name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onItemClick(int position) {
        Collection collection = collections.get(position);
        Toast.makeText(this, "Đã mở: " + collection.getName(), Toast.LENGTH_SHORT).show();
    }
}
