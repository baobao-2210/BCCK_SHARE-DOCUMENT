package com.example.bcck.data;

import androidx.annotation.NonNull;

import com.example.bcck.poster.Document;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SampleDocumentsSeeder {

    private SampleDocumentsSeeder() {
    }

    public interface SeedCallback {
        void onSuccess(int upsertedCount);

        void onError(@NonNull Exception error);
    }

    public static void seedComputerSciencePdfs(@NonNull FirebaseFirestore db, @NonNull SeedCallback callback) {
        Map<String, Document> samples = buildSamples();

        final int total = samples.size();
        final int[] successCount = {0};

        for (Map.Entry<String, Document> entry : samples.entrySet()) {
            db.collection("DocumentID")
                    .document(entry.getKey())
                    .set(entry.getValue())
                    .addOnSuccessListener(unused -> {
                        successCount[0]++;
                        if (successCount[0] == total) {
                            callback.onSuccess(total);
                        }
                    })
                    .addOnFailureListener(callback::onError);
        }
    }

    private static Map<String, Document> buildSamples() {
        Map<String, Document> map = new LinkedHashMap<>();

        map.put("sample_ods_java", build(
                "Open Data Structures (Java)",
                "https://opendatastructures.org/ods-java.pdf",
                "Cấu trúc dữ liệu & Giải thuật",
                "CNTT"
        ));
        map.put("sample_ods_cpp", build(
                "Open Data Structures (C++)",
                "https://opendatastructures.org/ods-cpp.pdf",
                "Cấu trúc dữ liệu & Giải thuật",
                "CNTT"
        ));
        map.put("sample_transformer", build(
                "Attention Is All You Need (Transformer)",
                "https://arxiv.org/pdf/1706.03762.pdf",
                "Trí tuệ nhân tạo / NLP",
                "CNTT"
        ));
        map.put("sample_resnet", build(
                "Deep Residual Learning for Image Recognition (ResNet)",
                "https://arxiv.org/pdf/1512.03385.pdf",
                "Thị giác máy tính",
                "CNTT"
        ));
        map.put("sample_adam", build(
                "Adam: A Method for Stochastic Optimization",
                "https://arxiv.org/pdf/1412.6980.pdf",
                "Machine Learning",
                "CNTT"
        ));
        map.put("sample_word2vec", build(
                "Distributed Representations of Words and Phrases (word2vec)",
                "https://arxiv.org/pdf/1310.4546.pdf",
                "NLP",
                "CNTT"
        ));

        return map;
    }

    private static Document build(String title, String url, String subject, String major) {
        Document document = new Document();
        document.setTitle(title);
        document.setDocType("PDF");
        document.setAuthorName("Sample");
        document.setSubject(subject);
        document.setTeacher("N/A");
        document.setMajor(major);
        document.setYear("2025-2026");
        document.setFileUrl(url);
        document.setUploaderId("sample");
        document.setUploaderName("sample@bcck");
        document.setUploadTimestamp(System.currentTimeMillis());
        document.setDownloads(0);
        document.setLikes(0);
        document.setRating(0f);
        document.setDescription(String.format(Locale.ROOT, "Sample PDF for %s", subject));
        return document;
    }
}

