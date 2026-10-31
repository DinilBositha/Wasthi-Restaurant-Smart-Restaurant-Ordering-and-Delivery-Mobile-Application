package com.dinilbositha.wasthirestaurant.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.MyApplication;
import com.dinilbositha.wasthirestaurant.database.AppDatabaseHelper;
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

    private AppDatabaseHelper getDbHelper() {
        if (MyApplication.getInstance() != null) {
            return AppDatabaseHelper.getInstance(MyApplication.getInstance());
        }
        return null;
    }

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
                            if (category.getCategoryId() == null || category.getCategoryId().isEmpty()) {
                                category.setCategoryId(doc.getId());
                            }
                            list.add(category);
                        }
                    }

                    // Save to SQLite offline database in background
                    AppDatabaseHelper dbHelper = getDbHelper();
                    if (dbHelper != null && !list.isEmpty()) {
                        new Thread(() -> dbHelper.saveCategories(list)).start();
                    }

                    result.setValue(Resource.success(list));
                })
                .addOnFailureListener(e -> {
                    // Fallback to SQLite cached categories if offline
                    AppDatabaseHelper dbHelper = getDbHelper();
                    if (dbHelper != null) {
                        List<Category> cached = dbHelper.getAllCachedCategories();
                        if (cached != null && !cached.isEmpty()) {
                            result.setValue(Resource.success(cached));
                            return;
                        }
                    }
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
                        activeCategoryIds.add(doc.getId());
                        Category category = doc.toObject(Category.class);
                        if (category != null && category.getCategoryId() != null && !category.getCategoryId().isEmpty()) {
                            activeCategoryIds.add(category.getCategoryId());
                        }
                    }

                    db.collection("products")
                            .whereEqualTo("status", true)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                List<Product> filteredList = new ArrayList<>();

                                for (DocumentSnapshot doc : productSnapshot.getDocuments()) {
                                    Product product = doc.toObject(Product.class);

                                    if (product != null) {
                                        if (product.getProductId() == null || product.getProductId().isEmpty()) {
                                            product.setProductId(doc.getId());
                                        }

                                        if (activeCategoryIds.isEmpty() ||
                                                product.getCategoryId() == null ||
                                                product.getCategoryId().isEmpty() ||
                                                activeCategoryIds.contains(product.getCategoryId())) {
                                            filteredList.add(product);
                                        }
                                    }
                                }

                                // Save to SQLite offline database in background
                                AppDatabaseHelper dbHelper = getDbHelper();
                                if (dbHelper != null && !filteredList.isEmpty()) {
                                    new Thread(() -> dbHelper.saveProducts(filteredList)).start();
                                }

                                result.setValue(Resource.success(filteredList));
                            })
                            .addOnFailureListener(e -> {
                                // Fallback to SQLite cached products
                                AppDatabaseHelper dbHelper = getDbHelper();
                                if (dbHelper != null) {
                                    List<Product> cached = dbHelper.getAllCachedProducts();
                                    if (cached != null && !cached.isEmpty()) {
                                        result.setValue(Resource.success(cached));
                                        return;
                                    }
                                }
                                result.setValue(Resource.error(e.getMessage()));
                            });
                })
                .addOnFailureListener(e -> {
                    // Fallback to SQLite cached products if offline
                    AppDatabaseHelper dbHelper = getDbHelper();
                    if (dbHelper != null) {
                        List<Product> cached = dbHelper.getAllCachedProducts();
                        if (cached != null && !cached.isEmpty()) {
                            result.setValue(Resource.success(cached));
                            return;
                        }
                    }
                    result.setValue(Resource.error(e.getMessage()));
                });

        return result;
    }
}