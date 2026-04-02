package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.model.AddressModel;
import com.dinilbositha.wasthirestaurant.repository.AddressRepository;

import java.util.List;

public class AddressViewModel extends ViewModel {

    private final AddressRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddressViewModel() {
        repository = new AddressRepository();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<List<AddressModel>> getAddresses() {
        return repository.getAddresses();
    }
    public void saveAddress(AddressModel addressModel) {
        loading.setValue(true);

        repository.saveAddress(addressModel, new AddressRepository.OnAddressSaveListener() {
            @Override
            public void onSuccess() {
                loading.postValue(false);
                saveSuccess.postValue(true);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }
    private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(false);

    public LiveData<Boolean> getDeleteSuccess() {
        return deleteSuccess;
    }

    public void deleteAddress(String addressId) {
        loading.setValue(true);

        repository.deleteAddress(addressId, new AddressRepository.OnAddressSaveListener() {
            @Override
            public void onSuccess() {
                loading.postValue(false);
                deleteSuccess.postValue(true);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void clearDeleteSuccess() {
        deleteSuccess.setValue(false);
    }
}