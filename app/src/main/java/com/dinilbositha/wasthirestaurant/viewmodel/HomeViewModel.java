package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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

    private final MediatorLiveData<Resource<List<HomeAds>>> promotions = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Category>>> categories = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Product>>> products = new MediatorLiveData<>();

    private LiveData<Resource<List<HomeAds>>> currentPromotionsSource;
    private LiveData<Resource<List<Category>>> currentCategoriesSource;
    private LiveData<Resource<List<Product>>> currentProductsSource;

    private boolean promotionsLoaded = false;
    private boolean categoriesLoaded = false;
    private boolean productsLoaded = false;
    private boolean hasLoadedOnce = false;

    private final MutableLiveData<Boolean> allDataLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    public HomeViewModel() {
        repository = new HomeRepository();
    }

    public void loadHomeDataIfNeeded() {
        if (currentPromotionsSource == null) {
            refreshHomeData();
        }
    }

    public void refreshHomeData() {
        promotionsLoaded = false;
        categoriesLoaded = false;
        productsLoaded = false;
        allDataLoaded.setValue(false);
        isRefreshing.setValue(true);

        if (currentPromotionsSource != null) promotions.removeSource(currentPromotionsSource);
        if (currentCategoriesSource != null) categories.removeSource(currentCategoriesSource);
        if (currentProductsSource != null) products.removeSource(currentProductsSource);

        currentPromotionsSource = repository.getPromotions();
        currentCategoriesSource = repository.getCategories();
        currentProductsSource = repository.getActiveProducts();

        promotions.addSource(currentPromotionsSource, promotions::setValue);
        categories.addSource(currentCategoriesSource, categories::setValue);
        products.addSource(currentProductsSource, products::setValue);
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

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
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
            isRefreshing.setValue(false);
        }
    }
}