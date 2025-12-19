package com.example.bcck.data;

import androidx.annotation.NonNull;

import com.example.bcck.poster.Document;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirestoreDocumentRepository {

    public interface LoadDocumentsCallback {
        void onSuccess(@NonNull List<Document> documents);

        void onError(@NonNull Exception error);
    }

    private static final String COLLECTION_DOCUMENTS = "DocumentID";

    private final FirebaseFirestore db;

    public FirestoreDocumentRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public FirestoreDocumentRepository(@NonNull FirebaseFirestore db) {
        this.db = db;
    }

    public void loadTopDocuments(
            @NonNull DocumentSort sort,
            int limit,
            @NonNull LoadDocumentsCallback callback
    ) {
        Query query = db.collection(COLLECTION_DOCUMENTS);

        switch (sort) {
            case POPULAR:
                query = query.orderBy("downloads", Query.Direction.DESCENDING);
                break;
            case NEWEST:
                query = query.orderBy("uploadTimestamp", Query.Direction.DESCENDING);
                break;
            case ALL:
            default:
                query = query.orderBy("title", Query.Direction.ASCENDING);
                break;
        }

        if (limit > 0) {
            query = query.limit(limit);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Document> results = new ArrayList<>();
                    for (DocumentSnapshot docSnap : queryDocumentSnapshots) {
                        Document doc = docSnap.toObject(Document.class);
                        if (doc != null) {
                            results.add(doc);
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(callback::onError);
    }
}

