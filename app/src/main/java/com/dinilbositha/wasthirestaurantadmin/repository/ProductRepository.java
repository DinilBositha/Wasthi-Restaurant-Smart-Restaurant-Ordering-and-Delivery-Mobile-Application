package com.dinilbositha.wasthirestaurantadmin.repository;

import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurantadmin.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final FirebaseFirestore firestore;

    public ProductRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getAllProducts(MutableLiveData<Boolean> loadingLiveData,
                               MutableLiveData<List<Product>> productsLiveData,
                               MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);

        firestore.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingLiveData.setValue(false);

                    List<Product> productList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            // Always use Firestore document id for update/delete operations
                            product.setProductId(document.getId());
                            productList.add(product);
                        }
                    }

                    productsLiveData.setValue(productList);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to load products."
                    );
                });
    }

    public void addProduct(Product product,
                           MutableLiveData<String> messageLiveData,
                           MutableLiveData<String> errorLiveData) {

        String docId = firestore.collection("products").document().getId();
        product.setProductId(docId);

        firestore.collection("products")
                .document(docId)
                .set(product)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Product added successfully.")
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to add product."
                        )
                );
    }

    public void updateProduct(Product product,
                              MutableLiveData<String> messageLiveData,
                              MutableLiveData<String> errorLiveData) {

        if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
            errorLiveData.setValue("Invalid product id.");
            return;
        }

        firestore.collection("products")
                .document(product.getProductId())
                .set(product)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Product updated successfully.")
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to update product."
                        )
                );
    }

    public void updateProductStatus(String productId,
                                    boolean status,
                                    MutableLiveData<String> messageLiveData,
                                    MutableLiveData<String> errorLiveData) {

        if (productId == null || productId.trim().isEmpty()) {
            errorLiveData.setValue("Invalid product id.");
            return;
        }

        firestore.collection("products")
                .document(productId)
                .update("status", status)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Product status updated successfully.")
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to update product status."
                        )
                );
    }
}