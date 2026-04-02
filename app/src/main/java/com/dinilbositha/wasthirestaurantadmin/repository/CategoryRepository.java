package com.dinilbositha.wasthirestaurantadmin.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurantadmin.model.Category;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {

    private final FirebaseFirestore firestore;

    public CategoryRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void getAllCategories(MutableLiveData<Boolean> loadingLiveData,
                                 MutableLiveData<List<Category>> categoriesLiveData,
                                 MutableLiveData<String> errorLiveData) {

        loadingLiveData.setValue(true);

        firestore.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingLiveData.setValue(false);

                    List<Category> categoryList = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Category category = document.toObject(Category.class);

                        if (category != null) {
                            category.setCategoryId(document.getId());
                            categoryList.add(category);
                        }
                    }

                    categoriesLiveData.setValue(categoryList);
                })
                .addOnFailureListener(e -> {
                    loadingLiveData.setValue(false);
                    Log.e("CategoryRepo", "FAILED: " + e.getMessage(), e);
                    errorLiveData.setValue(
                            e.getMessage() != null ? e.getMessage() : "Failed to load categories."
                    );
                });
    }

    public void addCategory(Category category,
                            MutableLiveData<String> messageLiveData,
                            MutableLiveData<String> errorLiveData) {

        String docId = firestore.collection("categories").document().getId();
        category.setCategoryId(docId);

        firestore.collection("categories")
                .document(docId)
                .set(category)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Category added successfully.")
                )
                .addOnFailureListener(e ->
                        errorLiveData.setValue(
                                e.getMessage() != null ? e.getMessage() : "Failed to add category."
                        )
                );
    }

    public void updateCategoryStatus(String categoryId,
                                     boolean status,
                                     MutableLiveData<String> messageLiveData,
                                     MutableLiveData<String> errorLiveData) {

        if (categoryId == null || categoryId.trim().isEmpty()) {
            errorLiveData.setValue("Invalid category id.");
            return;
        }

        firestore.collection("categories")
                .document(categoryId)
                .update("status", status)
                .addOnSuccessListener(unused ->
                        messageLiveData.setValue("Category status updated successfully.")
                )
                .addOnFailureListener(e -> {
                    Log.e("CategoryRepo", "STATUS UPDATE FAILED: " + e.getMessage(), e);
                    errorLiveData.setValue(
                            e.getMessage() != null ? e.getMessage() : "Failed to update category status."
                    );
                });
    }
}