package com.dinilbositha.wasthirestaurant.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dinilbositha.wasthirestaurant.activity.MainActivity;
import com.dinilbositha.wasthirestaurant.activity.MapPinActivity;
import com.dinilbositha.wasthirestaurant.databinding.FragmentAddAddressBinding;
import com.dinilbositha.wasthirestaurant.model.AddressModel;
import com.dinilbositha.wasthirestaurant.viewmodel.AddressViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddAddressFragment extends Fragment {

    private FragmentAddAddressBinding binding;
    private AddressViewModel addressViewModel;
    private FusedLocationProviderClient fusedLocationClient;

    private Double selectedLatitude = null;
    private Double selectedLongitude = null;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideMainNavigationUi();
        }
        setupClicks();
        observeViewModel();
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        binding.btnUseCurrentLocation.setOnClickListener(v -> checkLocationPermission());

        binding.btnPinOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapPinActivity.class);

            // If already has location, pass it
            if (selectedLatitude != null && selectedLongitude != null) {
                intent.putExtra("init_lat", selectedLatitude);
                intent.putExtra("init_lng", selectedLongitude);
            }

            mapPinLauncher.launch(intent);
        });

        binding.btnSaveAddress.setOnClickListener(v -> validateAndSaveAddress());
    }

    private void observeViewModel() {
        addressViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE);
            binding.btnSaveAddress.setEnabled(!Boolean.TRUE.equals(isLoading));
        });

        addressViewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Address saved successfully", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        addressViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    selectedLatitude = location.getLatitude();
                    selectedLongitude = location.getLongitude();
                    if (!isSriLankaLocation(selectedLatitude, selectedLongitude)) {
                        Toast.makeText(requireContext(), "Delivery available only within Sri Lanka", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    binding.txtSelectedLocation.setText(
                            "Location selected: " + selectedLatitude + ", " + selectedLongitude
                    );

                    fillAddressFromLocation(location);
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            );
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, addresses -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        setAddressFields(addresses.get(0));
                    }
                });
            } else {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    setAddressFields(addresses.get(0));
                }
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Unable to fetch address from location", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAddressFields(Address address) {
        if (binding == null) return;

        String city = address.getLocality();
        String addressLine = address.getAddressLine(0);

        if (!TextUtils.isEmpty(city) &&
                TextUtils.isEmpty(Objects.requireNonNull(binding.addAddressCity.getText()).toString().trim())) {
            binding.addAddressCity.setText(city);
        }

        if (!TextUtils.isEmpty(addressLine) &&
                TextUtils.isEmpty(Objects.requireNonNull(binding.addAddressAddressLine1.getText()).toString().trim())) {
            binding.addAddressAddressLine1.setText(addressLine);
        }
    }

    private void validateAndSaveAddress() {
        clearErrors();

        String title = Objects.requireNonNull(binding.addAddressTitle.getText()).toString().trim();
        String fullName = Objects.requireNonNull(binding.addAddressFullName.getText()).toString().trim();
        String mobileNumber = Objects.requireNonNull(binding.addAddressMobileNumber.getText()).toString().trim();
        String addressLine1 = Objects.requireNonNull(binding.addAddressAddressLine1.getText()).toString().trim();
        String addressLine2 = Objects.requireNonNull(binding.addAddressAddressLine2.getText()).toString().trim();
        String city = Objects.requireNonNull(binding.addAddressCity.getText()).toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.textInputLayoutTitle.setError("Enter title");
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            binding.textInputLayoutFullName.setError("Enter full name");
            return;
        }

        if (!isValidateMobile(mobileNumber)) {
            binding.textInputLayoutMobileNumber.setError("Enter valid Sri Lankan mobile number");
            return;
        }

        if (TextUtils.isEmpty(addressLine1)) {
            binding.textInputLayoutAddressLine1.setError("Enter address line 1");
            return;
        }

        if (TextUtils.isEmpty(city)) {
            binding.textInputLayoutCity.setError("Enter city");
            return;
        }

        AddressModel addressModel = AddressModel.builder()
                .title(title)
                .fullName(fullName)
                .mobileNumber(mobileNumber)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .city(city)
                .latitude(selectedLatitude)
                .longitude(selectedLongitude)
                .build();

        addressViewModel.saveAddress(addressModel);
    }

    private void clearErrors() {
        binding.textInputLayoutTitle.setError(null);
        binding.textInputLayoutFullName.setError(null);
        binding.textInputLayoutMobileNumber.setError(null);
        binding.textInputLayoutAddressLine1.setError(null);
        binding.textInputLayoutAddressLine2.setError(null);
        binding.textInputLayoutCity.setError(null);
    }
    private final ActivityResultLauncher<Intent> mapPinLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                    selectedLongitude = result.getData().getDoubleExtra("longitude", 0);

                    if (selectedLatitude != 0 && selectedLongitude != 0) {
                        binding.txtSelectedLocation.setText(
                                String.format("Location: %.6f, %.6f", selectedLatitude, selectedLongitude)
                        );
                    }
                }
            });
    public static Boolean isValidateMobile(String mobile) {
        return mobile != null && mobile.matches("^07[01245678]{1}[0-9]{7}$");
    }
    private boolean isSriLankaLocation(double latitude, double longitude) {
        return latitude >= 5.7 && latitude <= 9.9 &&
                longitude >= 79.5 && longitude <= 81.9;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMainNavigationUi();
        }
        binding = null;
    }
}