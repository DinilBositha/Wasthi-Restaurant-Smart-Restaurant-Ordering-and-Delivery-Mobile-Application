package com.dinilbositha.wasthirestaurant.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface OnOrderSaveListener {
        void onSuccess();
        void onFailure(String message);
    }

    public void saveOrder(@NonNull Order order, @NonNull OnOrderSaveListener listener) {
        firestore.collection("orders")
                .document(order.getOrderId())
                .set(order)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public LiveData<List<Order>> getUserOrders(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();

        firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orderList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orderList.add(order);
                        }
                    }

                    liveData.setValue(orderList);
                })
                .addOnFailureListener(e -> liveData.setValue(new ArrayList<>()));

        return liveData;
    }
}