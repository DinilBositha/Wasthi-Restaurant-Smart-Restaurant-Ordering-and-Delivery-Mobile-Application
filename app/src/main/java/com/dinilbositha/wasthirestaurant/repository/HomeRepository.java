package com.dinilbositha.wasthirestaurant.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.Category;
import com.dinilbositha.wasthirestaurant.model.HomeAds;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.dinilbositha.wasthirestaurant.utils.Resource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<List<HomeAds>>> getPromotions() {
        MutableLiveData<Resource<List<HomeAds>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        db.collection("promotions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<HomeAds> list = querySnapshot.toObjects(HomeAds.class);
                    result.setValue(Resource.success(list));
                })
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(e.getMessage()))
                );

        return result;
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        MutableLiveData<Resource<List<Category>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        db.collection("categories")
                .whereEqualTo("status", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> list = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Category category = doc.toObject(Category.class);
                        if (category != null) {
                            // keep stored categoryId if available, else fallback to doc id
                            if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
                                category.setCategoryId(doc.getId());
                            }
                            list.add(category);
                        }
                    }

                    result.setValue(Resource.success(list));
                })
                .addOnFailureListener(e -> {
                    result.setValue(Resource.error(e.getMessage()));
                });

        return result;
    }

    public LiveData<Resource<List<Product>>> getActiveProducts() {
        MutableLiveData<Resource<List<Product>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        db.collection("categories")
                .whereEqualTo("status", true)
                .get()
                .addOnSuccessListener(categorySnapshot -> {
                    Set<String> activeCategoryIds = new HashSet<>();

                    for (DocumentSnapshot doc : categorySnapshot.getDocuments()) {
                        Category category = doc.toObject(Category.class);
                        if (category != null) {
                            // use stored categoryId field first
                            if (category.getCategoryId() != null && !category.getCategoryId().isEmpty()) {
                                activeCategoryIds.add(category.getCategoryId());
                            } else {
                                // fallback only if categoryId field missing
                                activeCategoryIds.add(doc.getId());
                            }
                        }
                    }

                    db.collection("products")
                            .whereEqualTo("status", true)
                            .orderBy("createAt", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                List<Product> filteredList = new ArrayList<>();

                                for (DocumentSnapshot doc : productSnapshot.getDocuments()) {
                                    Product product = doc.toObject(Product.class);

                                    if (product != null) {
                                        if (product.getProductId() == null || product.getProductId().isEmpty()) {
                                            product.setProductId(doc.getId());
                                        }

                                        if (product.getCategoryId() != null &&
                                                activeCategoryIds.contains(product.getCategoryId())) {
                                            filteredList.add(product);
                                        }
                                    }
                                }

                                result.setValue(Resource.success(filteredList));
                            })
                            .addOnFailureListener(e ->
                                    result.setValue(Resource.error(e.getMessage()))
                            );
                })
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(e.getMessage()))
                );

        return result;
    }
}