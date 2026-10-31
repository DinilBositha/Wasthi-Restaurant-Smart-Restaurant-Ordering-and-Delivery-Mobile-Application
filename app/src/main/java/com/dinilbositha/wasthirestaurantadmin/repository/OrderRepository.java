package com.dinilbositha.wasthirestaurantadmin.repository;

import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurantadmin.model.Order;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private static final int PAGE_SIZE = 10;

    private final FirebaseFirestore firestore;
    private DocumentSnapshot lastVisibleDocument;
    private boolean isLastPage = false;

    public OrderRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void resetPagination() {
        lastVisibleDocument = null;
        isLastPage = false;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void getOrders(boolean isFirstPage,
                          MutableLiveData<Boolean> loadingLiveData,
                          MutableLiveData<List<Order>> ordersLiveData,
                          MutableLiveData<String> errorLiveData) {

        if (isFirstPage) {
            resetPagination();
        }

        if (isLastPage) {
            return;
        }

        loadingLiveData.setValue(true);

        Query query = firestore.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (!isFirstPage && lastVisibleDocument != null) {
            query = query.startAfter(lastVisibleDocument);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingLiveData.setValue(false);

                    List<Order> orderList = new ArrayList<>();
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    if (!documents.isEmpty()) {
                        lastVisibleDocument = documents.get(documents.size() - 1);
                    }

                    if (documents.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }

                    for (DocumentSnapshot document : documents) {
                        Order order = document.toObject(Order.class);

                        if (order != null) {
                            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                                order.setOrderId(document.getId());
                            }
                            orderList.add(order);
                        }
                    }

                    ordersLiveData.setValue(orderList);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Failed to load orders.");
                });
    }

    public void updateOrderStatus(String orderId,
                                  String newStatus,
                                  MutableLiveData<String> messageLiveData,
                                  MutableLiveData<String> errorLiveData) {

        firestore.collection("orders")
                .document(orderId)
                .update("orderStatus", newStatus)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Order status updated successfully.")
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue("Failed to update order status.")
                );
    }
}