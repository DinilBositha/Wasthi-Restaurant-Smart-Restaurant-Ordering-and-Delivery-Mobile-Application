package com.dinilbositha.wasthirestaurantadmin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurantadmin.model.User;
import com.dinilbositha.wasthirestaurantadmin.repository.UserRepository;

import java.util.List;

public class UserViewModel extends ViewModel {

    private final UserRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public UserViewModel() {
        repository = new UserRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void loadUsers() {
        repository.getAllUsers(loading, users, error);
    }

    public void updateUserStatus(String uid, boolean active) {
        repository.updateUserStatus(uid, active, message, error);
    }
}

