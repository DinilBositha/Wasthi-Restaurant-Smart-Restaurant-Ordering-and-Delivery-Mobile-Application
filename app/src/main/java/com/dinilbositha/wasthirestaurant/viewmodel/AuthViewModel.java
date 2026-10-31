package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.repository.AuthRepository;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> resetPasswordSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<Boolean> getResetPasswordSuccess() {
        return resetPasswordSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loginUser(String email, String password) {
        loginSuccess.setValue(false);
        errorMessage.setValue(null);
        authRepository.loginUser(email, password, loading, loginSuccess, errorMessage);
    }

    public void registerUser(String name, String email, String password) {
        registerSuccess.setValue(false);
        errorMessage.setValue(null);
        authRepository.registerUser(name, email, password, loading, registerSuccess, errorMessage);
    }

    public void resetPassword(String email) {
        resetPasswordSuccess.setValue(false);
        errorMessage.setValue(null);
        authRepository.resetPassword(email, loading, resetPasswordSuccess, errorMessage);
    }

    public void clearLoginSuccess() {
        loginSuccess.setValue(false);
    }

    public void clearRegisterSuccess() {
        registerSuccess.setValue(false);
    }

    public void clearResetPasswordSuccess() {
        resetPasswordSuccess.setValue(false);
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
}