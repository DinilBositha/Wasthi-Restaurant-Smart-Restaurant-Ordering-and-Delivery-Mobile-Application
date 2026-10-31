package com.dinilbositha.wasthirestaurant.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.location.Priority;

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

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    getCurrentLocation();
                } else {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Location permission denied. Please select location on map.", Toast.LENGTH_SHORT).show();
                    }
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
        if (getContext() == null) return;
        boolean hasFine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFine || hasCoarse) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (!isAdded() || getContext() == null) return;

        Context context = getContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!isGpsEnabled) {
            Toast.makeText(requireContext(), "Please enable GPS / Location on your device", Toast.LENGTH_LONG).show();
        }

        if (binding != null) {
            binding.txtSelectedLocation.setText("Detecting current location...");
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            handleLocationResult(location);
                        } else {
                            // Fallback to last known location
                            try {
                                fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                                    if (lastLoc != null) {
                                        handleLocationResult(lastLoc);
                                    } else {
                                        if (isAdded() && getContext() != null) {
                                            if (binding != null) {
                                                binding.txtSelectedLocation.setText(selectedLatitude != null ? String.format(Locale.getDefault(), "Location: %.6f, %.6f", selectedLatitude, selectedLongitude) : "Tap 'Select on Map' to pin location");
                                            }
                                            Toast.makeText(requireContext(), "Could not detect GPS fix. Please select your address on the map.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }).addOnFailureListener(e -> {
                                    if (isAdded() && getContext() != null) {
                                        Toast.makeText(requireContext(), "GPS location unavailable. Please select on map.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (SecurityException ignored) {
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                                if (lastLoc != null) {
                                    handleLocationResult(lastLoc);
                                } else if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "GPS location unavailable. Please select on map.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (SecurityException ignored) {
                        }
                    });
        } catch (SecurityException e) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleLocationResult(Location location) {
        if (!isAdded() || getContext() == null || location == null) return;

        selectedLatitude = location.getLatitude();
        selectedLongitude = location.getLongitude();

        if (!isSriLankaLocation(selectedLatitude, selectedLongitude)) {
            Toast.makeText(requireContext(), "Current GPS location is outside Sri Lanka. Please pin location on map.", Toast.LENGTH_LONG).show();
            if (binding != null) {
                binding.txtSelectedLocation.setText("Selected location outside Sri Lanka");
            }
            return;
        }

        if (binding != null) {
            binding.txtSelectedLocation.setText(
                    String.format(Locale.getDefault(), "Location selected: %.6f, %.6f", selectedLatitude, selectedLongitude)
            );
        }

        fillAddressFromLocation(location);
    }

    private void fillAddressFromLocation(Location location) {
        if (location == null || getContext() == null) return;
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1, addresses -> {
                    if (addresses != null && !addresses.isEmpty() && isAdded() && getActivity() != null) {
                        requireActivity().runOnUiThread(() -> setAddressFields(addresses.get(0)));
                    }
                });
            } else {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    setAddressFields(addresses.get(0));
                }
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to fetch address from location", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAddressFields(Address address) {
        if (binding == null) return;

        String city = address.getLocality();
        if (TextUtils.isEmpty(city)) {
            city = address.getSubAdminArea();
        }
        if (TextUtils.isEmpty(city)) {
            city = address.getAdminArea();
        }

        String addressLine = address.getAddressLine(0);

        if (!TextUtils.isEmpty(city)) {
            binding.addAddressCity.setText(city);
        }

        if (!TextUtils.isEmpty(addressLine)) {
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
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    selectedLatitude = result.getData().getDoubleExtra("latitude", 0);
                    selectedLongitude = result.getData().getDoubleExtra("longitude", 0);

                    if (selectedLatitude != 0 && selectedLongitude != 0) {
                        binding.txtSelectedLocation.setText(
                                String.format(Locale.getDefault(), "Location: %.6f, %.6f", selectedLatitude, selectedLongitude)
                        );

                        Location loc = new Location("");
                        loc.setLatitude(selectedLatitude);
                        loc.setLongitude(selectedLongitude);
                        fillAddressFromLocation(loc);
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