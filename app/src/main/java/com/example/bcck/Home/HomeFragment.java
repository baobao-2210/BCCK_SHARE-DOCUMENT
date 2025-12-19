package com.example.bcck.Home;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

// Import thêm SearchView
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;
import com.example.bcck.data.DocumentSort;
import com.example.bcck.data.FirestoreDocumentRepository;
import com.example.bcck.data.SampleDocumentsSeeder;
import com.example.bcck.poster.Document;
import com.example.bcck.poster.DocumentAdapter;
import com.example.bcck.poster.DocumentDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Cloud Storage Cards
    private CardView cardDropbox, cardGDrive, cardOneDrive1, cardOneDrive2;

    // Filter Buttons
    private Button btnAll, btnPopular, btnNewest;

    // Search View
    private SearchView searchView; // Biến mới cho tìm kiếm

    private RecyclerView recyclerViewHomeDocuments;
    private DocumentAdapter documentAdapter;

    // homeDocuments: Danh sách đang hiển thị trên màn hình
    private final List<Document> homeDocuments = new ArrayList<>();

    // originalDocuments: Danh sách gốc để backup (dùng khi tìm kiếm)
    private final List<Document> originalDocuments = new ArrayList<>();

    private ImageView addIcon;

    private final FirestoreDocumentRepository documentRepository = new FirestoreDocumentRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);

        // Gắn sự kiện cho nút chuông
        ImageView iconNoti = view.findViewById(R.id.iconNotification);
        iconNoti.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        setupCloudStorageCards();
        setupFilterButtons();
        setupRecyclerView();
        setupSeedButton();

        // Cài đặt chức năng tìm kiếm
        setupSearchView();

        // Tải dữ liệu ban đầu
        loadTopDocuments(DocumentSort.ALL);

        return view;
    }

    private void initViews(View view) {
        cardDropbox = view.findViewById(R.id.cardDropbox);
        cardGDrive = view.findViewById(R.id.cardGDrive);
        cardOneDrive1 = view.findViewById(R.id.cardOneDrive1);
        cardOneDrive2 = view.findViewById(R.id.cardOneDrive2);

        btnAll = view.findViewById(R.id.btnAll);
        btnPopular = view.findViewById(R.id.btnPopular);
        btnNewest = view.findViewById(R.id.btnNewest);

        // Ánh xạ SearchView từ XML
        searchView = view.findViewById(R.id.searchView);

        addIcon = view.findViewById(R.id.addIcon);
        recyclerViewHomeDocuments = view.findViewById(R.id.recyclerViewHomeDocuments);
    }

    // --- HÀM MỚI: Cấu hình tìm kiếm ---
    private void setupSearchView() {
        if (searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDocuments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Tìm kiếm ngay khi gõ chữ
                filterDocuments(newText);
                return true;
            }
        });
    }

    // --- HÀM MỚI: Logic lọc danh sách ---
    private void filterDocuments(String query) {
        homeDocuments.clear(); // Xóa danh sách hiển thị hiện tại

        if (query == null || query.trim().isEmpty()) {
            // Nếu ô tìm kiếm rỗng, hiển thị lại toàn bộ danh sách gốc
            homeDocuments.addAll(originalDocuments);
        } else {
            // Nếu có từ khóa, duyệt qua danh sách gốc để tìm file phù hợp
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Document doc : originalDocuments) {
                // Tìm theo Tên tài liệu hoặc Tên môn học
                if (doc.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        (doc.getSubject() != null && doc.getSubject().toLowerCase().contains(lowerCaseQuery))) {
                    homeDocuments.add(doc);
                }
            }
        }
        // Cập nhật giao diện
        documentAdapter.notifyDataSetChanged();
    }

    private void setupCloudStorageCards() {
        cardDropbox.setOnClickListener(v -> Toast.makeText(getContext(), "Đang mở Dropbox...", Toast.LENGTH_SHORT).show());
        cardGDrive.setOnClickListener(v -> Toast.makeText(getContext(), "Đang mở Google Drive...", Toast.LENGTH_SHORT).show());
        cardOneDrive1.setOnClickListener(v -> Toast.makeText(getContext(), "Đang mở Nirwna - OneDrive...", Toast.LENGTH_SHORT).show());
        cardOneDrive2.setOnClickListener(v -> Toast.makeText(getContext(), "Đang mở PIDT - OneDrive...", Toast.LENGTH_SHORT).show());
    }

    private void setupFilterButtons() {
        btnAll.setOnClickListener(v -> {
            selectFilterButton(btnAll);
            loadTopDocuments(DocumentSort.ALL);
        });

        btnPopular.setOnClickListener(v -> {
            selectFilterButton(btnPopular);
            loadTopDocuments(DocumentSort.POPULAR);
        });

        btnNewest.setOnClickListener(v -> {
            selectFilterButton(btnNewest);
            loadTopDocuments(DocumentSort.NEWEST);
        });
    }

    private void selectFilterButton(Button selectedButton) {
        btnAll.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        btnAll.setTextColor(0xFF000000);

        btnPopular.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        btnPopular.setTextColor(0xFF000000);

        btnNewest.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
        btnNewest.setTextColor(0xFF000000);

        selectedButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark, null));
        selectedButton.setTextColor(0xFFFFFFFF);
    }

    private void setupRecyclerView() {
        recyclerViewHomeDocuments.setLayoutManager(new LinearLayoutManager(getContext()));
        documentAdapter = new DocumentAdapter(homeDocuments, new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(Document document) {
                openDocumentDetail(document);
            }

            @Override
            public void onMoreClick(Document document) {
                Toast.makeText(getContext(), "Tùy chọn: " + document.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewHomeDocuments.setAdapter(documentAdapter);
    }

    private void setupSeedButton() {
        if (addIcon == null) return;
        addIcon.setOnClickListener(v -> {
            boolean isDebuggable = false;
            if (getContext() != null) {
                int flags = getContext().getApplicationInfo().flags;
                isDebuggable = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }

            if (!isDebuggable) {
                Toast.makeText(getContext(), "Chức năng này chỉ bật trong debug.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Đang tạo dữ liệu mẫu...", Toast.LENGTH_SHORT).show();
            SampleDocumentsSeeder.seedComputerSciencePdfs(FirebaseFirestore.getInstance(), new SampleDocumentsSeeder.SeedCallback() {
                @Override
                public void onSuccess(int upsertedCount) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Đã tạo " + upsertedCount + " tài liệu.", Toast.LENGTH_SHORT).show();
                    selectFilterButton(btnNewest);
                    loadTopDocuments(DocumentSort.NEWEST);
                }

                @Override
                public void onError(@NonNull Exception error) {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // --- CẬP NHẬT HÀM NÀY: Lưu dữ liệu vào cả 2 danh sách ---
    private void loadTopDocuments(@NonNull DocumentSort sort) {
        // Reset ô tìm kiếm khi đổi bộ lọc (Sort)
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }

        documentRepository.loadTopDocuments(sort, 30, new FirestoreDocumentRepository.LoadDocumentsCallback() {
            @Override
            public void onSuccess(@NonNull List<Document> documents) {
                if (!isAdded()) return;

                // 1. Cập nhật danh sách hiển thị
                homeDocuments.clear();
                homeDocuments.addAll(documents);

                // 2. Cập nhật danh sách gốc (Backup) để dùng cho tìm kiếm
                originalDocuments.clear();
                originalDocuments.addAll(documents);

                documentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Exception error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openDocumentDetail(@NonNull Document document) {
        Intent intent = new Intent(getActivity(), DocumentDetailActivity.class);
        intent.putExtra(DocumentDetailActivity.EXTRA_DOCUMENT, document);
        startActivity(intent);
    }
}