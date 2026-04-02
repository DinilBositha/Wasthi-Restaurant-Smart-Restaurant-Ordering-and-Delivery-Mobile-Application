package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.model.Category;
import com.dinilbositha.wasthirestaurant.model.HomeAds;
import com.dinilbositha.wasthirestaurant.model.Product;
import com.dinilbositha.wasthirestaurant.repository.HomeRepository;
import com.dinilbositha.wasthirestaurant.utils.Resource;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final HomeRepository repository;

    private LiveData<Resource<List<HomeAds>>> promotions;
    private LiveData<Resource<List<Category>>> categories;
    private LiveData<Resource<List<Product>>> products;

    private boolean promotionsLoaded = false;
    private boolean categoriesLoaded = false;
    private boolean productsLoaded = false;
    private boolean hasLoadedOnce = false;

    private final MutableLiveData<Boolean> allDataLoaded = new MutableLiveData<>(false);

    public HomeViewModel() {
        repository = new HomeRepository();
    }

    public void loadHomeDataIfNeeded() {
        if (promotions == null) {
            promotions = repository.getPromotions();
        }
        if (categories == null) {
            categories = repository.getCategories();
        }
        if (products == null) {
            products = repository.getActiveProducts();
        }
    }

    public void refreshHomeData() {
        promotionsLoaded = false;
        categoriesLoaded = false;
        productsLoaded = false;
        allDataLoaded.setValue(false);

        promotions = repository.getPromotions();
        categories = repository.getCategories();
        products = repository.getActiveProducts();
    }

    public LiveData<Resource<List<HomeAds>>> getPromotions() {
        return promotions;
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        return categories;
    }

    public LiveData<Resource<List<Product>>> getActiveProducts() {
        return products;
    }

    public void setPromotionsLoaded(boolean loaded) {
        promotionsLoaded = loaded;
        updateAllLoadedState();
    }

    public void setCategoriesLoaded(boolean loaded) {
        categoriesLoaded = loaded;
        updateAllLoadedState();
    }

    public void setProductsLoaded(boolean loaded) {
        productsLoaded = loaded;
        updateAllLoadedState();
    }

    public boolean isAllLoaded() {
        return promotionsLoaded && categoriesLoaded && productsLoaded;
    }

    public LiveData<Boolean> getAllDataLoaded() {
        return allDataLoaded;
    }

    public boolean hasLoadedOnce() {
        return hasLoadedOnce;
    }

    private void updateAllLoadedState() {
        boolean loaded = isAllLoaded();
        allDataLoaded.setValue(loaded);

        if (loaded) {
            hasLoadedOnce = true;
        }
    }
}