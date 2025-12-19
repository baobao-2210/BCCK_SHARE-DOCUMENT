package com.example.bcck.poster;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;

import com.example.bcck.Home.NotificationActivity;
import com.example.bcck.HomeActivity;
import com.example.bcck.Profile.ProfileActivity;
import com.example.bcck.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DocumentFragment extends Fragment {

    private RecyclerView recyclerViewDocuments;
    private DocumentAdapter documentAdapter;
    private List<Document> documentList;
    private List<Document> filteredDocumentList;

    private EditText searchBox;
    private ImageView logoIcon, iconNotification, iconProfile;
    private CardView btnUpload;
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupButtons();
        loadDocumentsFromFirestore();
        return view;


    }

    private void initViews(View view) {
        recyclerViewDocuments = view.findViewById(R.id.recyclerViewDocuments);
        searchBox = view.findViewById(R.id.searchBox);
        iconNotification = view.findViewById(R.id.iconNotification);
        iconProfile = view.findViewById(R.id.iconProfile);
        btnUpload = view.findViewById(R.id.btnUpload);
        logoIcon = view.findViewById(R.id.logoIcon);
        db = FirebaseFirestore.getInstance();

    }

    private void setupRecyclerView() {
        recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        documentList = new ArrayList<>();
        filteredDocumentList = new ArrayList<>();

        documentAdapter = new DocumentAdapter(filteredDocumentList, new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(Document document) {
                // **ĐOẠN CODE CẦN THAY THẾ TOAST**
                openDocumentDetail(document);
            }
            @Override
            public void onMoreClick(Document document) {
                // Hiển thị menu options
                Toast.makeText(getContext(), "Options cho: " + document.getTitle(), Toast.LENGTH_SHORT).show();
                // TODO: Hiển thị Bottom Sheet với options: Download, Share, Report...
            }
        });
        recyclerViewDocuments.setAdapter(documentAdapter);
    }

    // 2. TẠO HÀM MỚI ĐỂ XỬ LÝ VIỆC CHUYỂN MÀN HÌNH
    private void openDocumentDetail(Document document) {
        // Đảm bảo getContext() không null (luôn kiểm tra trong Fragment)
        if (getContext() != null) {
            // 1. Tạo Intent và chỉ định DocumentDetailActivity
            Intent intent = new Intent(getContext(), DocumentDetailActivity.class);

            // 2. Đính kèm đối tượng Document (BẮT BUỘC Document phải là Parcelable)
            // Đảm bảo "DOCUMENT_DETAIL" khớp với key bạn dùng trong DocumentDetailActivity
            intent.putExtra("DOCUMENT_DETAIL", document);

            // 3. Khởi chạy Activity
            startActivity(intent);
        }
    }


    private void setupSearch() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDocuments(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons() {
        // 1. Xử lý nút Thông Báo (Bell)
        iconNotification.setOnClickListener(v -> {
            // Chuyển sang màn hình NotificationActivity
            // Bạn cần tạo Activity này nếu chưa có (Xem hướng dẫn Bước 1)
            Intent intent = new Intent(getContext(), NotificationActivity.class);
            startActivity(intent);
        });

        // 2. Xử lý nút Profile (Cá nhân)
        iconProfile.setOnClickListener(v -> {
            // Cách mới: Chuyển sang Activity Profile riêng biệt -> KHÔNG BAO GIỜ CRASH
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        // 3. Xử lý nút Upload (Đã có)
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.bcck.poster.UploadDocumentActivity.class);
            startActivity(intent);
        });
        // --- 4. Xử lý nút Logo (Về trang chủ) --
        logoIcon.setOnClickListener(v -> {
            // Thay 'MainActivity.class' bằng tên file Activity trang chủ của bạn
            Intent intent = new Intent(requireContext(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void loadDocumentsFromFirestore() {
        db.collection("DocumentID")

                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    documentList.clear();

                    for (DocumentSnapshot docSnap : queryDocumentSnapshots) {

                        Document doc = docSnap.toObject(Document.class);

                        if (doc != null) {
                            documentList.add(doc);
                        }
                    }

                    filteredDocumentList.clear();
                    filteredDocumentList.addAll(documentList);
                    documentAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void filterDocuments(String query) {
        filteredDocumentList.clear();

        if (query.isEmpty()) {
            filteredDocumentList.addAll(documentList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Document doc : documentList) {
                if (doc.getTitle().toLowerCase().contains(lowerQuery) ||
                        doc.getAuthorName().toLowerCase().contains(lowerQuery) ||
                        doc.getSubject().toLowerCase().contains(lowerQuery)) {
                    filteredDocumentList.add(doc);
                }
            }
        }
        documentAdapter.notifyDataSetChanged();
    }
}
