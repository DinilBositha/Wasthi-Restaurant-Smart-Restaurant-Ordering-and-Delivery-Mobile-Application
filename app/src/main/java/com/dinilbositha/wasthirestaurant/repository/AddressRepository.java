package com.dinilbositha.wasthirestaurant.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dinilbositha.wasthirestaurant.model.AddressModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddressRepository {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public AddressRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public interface OnAddressSaveListener {
        void onSuccess();
        void onFailure(String message);
    }

    public void saveAddress(AddressModel addressModel, @NonNull OnAddressSaveListener listener) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = currentUser.getUid();
        String addressId = firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .document()
                .getId();

        addressModel.setId(addressId);

        firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .document(addressId)
                .set(addressModel)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public LiveData<List<AddressModel>> getAddresses() {
        MutableLiveData<List<AddressModel>> addressesLiveData = new MutableLiveData<>();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            addressesLiveData.setValue(new ArrayList<>());
            return addressesLiveData;
        }

        String uid = currentUser.getUid();

        firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AddressModel> addressList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        AddressModel address = doc.toObject(AddressModel.class);
                        if (address != null) {
                            address.setId(doc.getId());
                            addressList.add(address);
                        }
                    }

                    addressesLiveData.setValue(addressList);
                })
                .addOnFailureListener(e -> addressesLiveData.setValue(new ArrayList<>()));

        return addressesLiveData;
    }
    public void deleteAddress(String addressId, @NonNull OnAddressSaveListener listener) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = currentUser.getUid();

        firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .document(addressId)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(
                        e.getMessage() != null ? e.getMessage() : "Failed to delete address"
                ));
    }
}