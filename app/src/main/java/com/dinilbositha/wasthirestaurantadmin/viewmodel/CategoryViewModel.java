package com.dinilbositha.wasthirestaurantadmin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurantadmin.model.Category;
import com.dinilbositha.wasthirestaurantadmin.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public CategoryViewModel() {
        repository = new CategoryRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadCategories() {
        repository.getAllCategories(loading, categories, error);
    }

    public void addCategory(Category category) {
        repository.addCategory(category, message, error);
    }

    public void updateCategoryStatus(String categoryId, boolean status) {
        repository.updateCategoryStatus(categoryId, status, message, error);
    }

    public void clearMessage() {
        message.setValue(null);
    }

    public void clearError() {
        error.setValue(null);
    }
}