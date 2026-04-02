package com.dinilbositha.wasthirestaurantadmin.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.dinilbositha.wasthirestaurantadmin.model.DashboardStats;

public class HomeRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<DashboardStats> getDashboardStats() {
        MutableLiveData<DashboardStats> liveData = new MutableLiveData<>();
        DashboardStats stats = new DashboardStats();

        db.collection("orders").get().addOnSuccessListener(orderSnapshot -> {
            stats.setTotalOrders(orderSnapshot.size());

            int pendingCount = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : orderSnapshot.getDocuments()) {
                String status = doc.getString("orderStatus");
                if ("Pending".equalsIgnoreCase(status)) {
                    pendingCount++;
                }
            }
            stats.setPendingOrders(pendingCount);

            db.collection("products").get().addOnSuccessListener(productSnapshot -> {
                stats.setProductsCount(productSnapshot.size());

                db.collection("users").get().addOnSuccessListener(userSnapshot -> {
                    stats.setUsersCount(userSnapshot.size());
                    liveData.setValue(stats);
                });
            });
        });

        return liveData;
    }
}