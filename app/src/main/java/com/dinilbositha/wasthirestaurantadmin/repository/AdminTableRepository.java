package com.dinilbositha.wasthirestaurantadmin.repository;

import com.dinilbositha.wasthirestaurantadmin.model.TableModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminTableRepository {

    public interface TableInsertCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void insertTable(TableModel tableModel, TableInsertCallback callback) {
        if (tableModel == null || tableModel.getTableNumber() == null || tableModel.getTableNumber().trim().isEmpty()) {
            callback.onFailure("Invalid table data");
            return;
        }

        String documentId = tableModel.getTableNumber().trim();

        firestore.collection("tables")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onFailure("Table number already exists");
                    } else {
                        firestore.collection("tables")
                                .document(documentId)
                                .set(tableModel)
                                .addOnSuccessListener(unused -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onFailure(
                                        e.getMessage() != null ? e.getMessage() : "Failed to save table"
                                ));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(
                        e.getMessage() != null ? e.getMessage() : "Failed to check table"
                ));
    }
}