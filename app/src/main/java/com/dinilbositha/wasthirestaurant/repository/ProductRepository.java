package com.dinilbositha.wasthirestaurant.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Product> getProductById(String productId) {
        MutableLiveData<Product> productLiveData = new MutableLiveData<>();

        db.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Product product = queryDocumentSnapshots.getDocuments()
                                .get(0)
                                .toObject(Product.class);

                        if (product != null && product.isStatus()) {
                            product.setProductVariants(filterActiveVariants(product.getProductVariants()));
                            productLiveData.setValue(product);
                        } else {
                            productLiveData.setValue(null);
                        }
                    } else {
                        productLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> productLiveData.setValue(null));

        return productLiveData;
    }

    public LiveData<List<Product>> getRelatedProducts(String categoryId, String currentProductId) {
        MutableLiveData<List<Product>> relatedProductsLiveData = new MutableLiveData<>();

        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> relatedList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);

                        if (product != null
                                && product.isStatus()
                                && product.getProductId() != null
                                && !product.getProductId().equals(currentProductId)) {

                            product.setProductVariants(filterActiveVariants(product.getProductVariants()));
                            relatedList.add(product);
                        }
                    }

                    if (relatedList.size() > 5) {
                        relatedList = relatedList.subList(0, 5);
                    }

                    relatedProductsLiveData.setValue(relatedList);
                })
                .addOnFailureListener(e -> relatedProductsLiveData.setValue(new ArrayList<>()));

        return relatedProductsLiveData;
    }

    public LiveData<List<Product>> getMoreProducts(String currentProductId, String currentCategoryId) {
        MutableLiveData<List<Product>> moreProductsLiveData = new MutableLiveData<>();

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> moreList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);

                        if (product != null
                                && product.isStatus()
                                && product.getProductId() != null
                                && !product.getProductId().equals(currentProductId)
                                && product.getCategoryId() != null
                                && !product.getCategoryId().equals(currentCategoryId)) {

                            product.setProductVariants(filterActiveVariants(product.getProductVariants()));
                            moreList.add(product);
                        }
                    }

                    if (moreList.size() > 10) {
                        moreList = moreList.subList(0, 10);
                    }

                    moreProductsLiveData.setValue(moreList);
                })
                .addOnFailureListener(e -> moreProductsLiveData.setValue(new ArrayList<>()));

        return moreProductsLiveData;
    }

    private List<Product.ProductVariant> filterActiveVariants(List<Product.ProductVariant> variants) {
        List<Product.ProductVariant> activeVariants = new ArrayList<>();

        if (variants != null) {
            for (Product.ProductVariant variant : variants) {
                if (variant != null && variant.isVariantStatus()) {
                    activeVariants.add(variant);
                }
            }
        }

        return activeVariants;
    }
}