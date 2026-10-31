package com.dinilbositha.wasthirestaurant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dinilbositha.wasthirestaurant.model.TableModel;
import com.dinilbositha.wasthirestaurant.repository.TableRepository;

public class TableViewModel extends ViewModel {

    private final MutableLiveData<TableModel> selectedTable = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final TableRepository repository = new TableRepository();

    public LiveData<TableModel> getSelectedTable() {
        return selectedTable;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void validateAndSelectTable(String scannedQr) {
        loading.setValue(true);

        repository.validateTableQr(scannedQr, new TableRepository.TableValidationCallback() {
            @Override
            public void onSuccess(TableModel table) {
                loading.postValue(false);
                selectedTable.postValue(table);
            }

            @Override
            public void onFailure(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    public void clearSelectedTable() {
        selectedTable.setValue(null);
    }
}