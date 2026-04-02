package com.dinilbositha.wasthirestaurant.repository;

import com.dinilbositha.wasthirestaurant.model.TableModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TableRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface TableValidationCallback {
        void onSuccess(TableModel table);
        void onFailure(String message);
    }

    public void validateTableQr(String scannedQr, TableValidationCallback callback) {
        firestore.collection("tables")
                .whereEqualTo("qrValue", scannedQr)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        callback.onFailure("Invalid table QR");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TableModel table = document.toObject(TableModel.class);
                        if (table != null) {
                            callback.onSuccess(table);
                            return;
                        }
                    }

                    callback.onFailure("Invalid table QR");
                })
                .addOnFailureListener(e -> {
                    String error = e.getMessage() != null ? e.getMessage() : "Failed to validate table";
                    callback.onFailure(error);
                });
    }
}