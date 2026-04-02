package com.dinilbositha.wasthirestaurant.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dinilbositha.wasthirestaurant.R;
import com.dinilbositha.wasthirestaurant.databinding.ActivityMapPinBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapPinActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapPinBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private Double selectedLat = null;
    private Double selectedLng = null;

    // Sri Lanka bounds
    private static final double SL_LAT_MIN = 5.7;
    private static final double SL_LAT_MAX = 9.9;
    private static final double SL_LNG_MIN = 79.5;
    private static final double SL_LNG_MAX = 81.9;

    // Default location - Colombo
    private static final LatLng DEFAULT_LOCATION = new LatLng(6.9271, 79.8612);

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    moveToCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupClicks();
    }

    private void setupClicks() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLat != null && selectedLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLat);
                resultIntent.putExtra("longitude", selectedLng);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnMyLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                moveToCurrentLocation();
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
//        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(getApplicationContext(),R.raw.map_style);
//        mMap.setMapStyle(styleOptions);
        // Default camera to Sri Lanka
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 8f));

        // Map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        // Map click listener
        mMap.setOnMapClickListener(latLng -> {
            double lat = latLng.latitude;
            double lng = latLng.longitude;

            // Sri Lanka check
            if (!isSriLankaLocation(lat, lng)) {
                Toast.makeText(this, "Please select a location within Sri Lanka", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedLat = lat;
            selectedLng = lng;

            // Clear old marker
            mMap.clear();

            // Add new marker
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Delivery Location"));

            // Move camera
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

            // Update UI
            binding.txtSelectedLocation.setText(
                    String.format("Selected: %.6f, %.6f", selectedLat, selectedLng)
            );
            binding.btnConfirmLocation.setEnabled(true);
        });

        // Check if location permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }

        // Check if initial lat/lng passed
        double initLat = getIntent().getDoubleExtra("init_lat", 0);
        double initLng = getIntent().getDoubleExtra("init_lng", 0);

        if (initLat != 0 && initLng != 0) {
            LatLng initPos = new LatLng(initLat, initLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPos, 16f));
            mMap.addMarker(new MarkerOptions().position(initPos).title("Previous Location"));
            selectedLat = initLat;
            selectedLng = initLng;
            binding.btnConfirmLocation.setEnabled(true);
            binding.txtSelectedLocation.setText(
                    String.format("Selected: %.6f, %.6f", initLat, initLng)
            );
        }
    }

    private void moveToCurrentLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            if (!isSriLankaLocation(lat, lng)) {
                                Toast.makeText(this, "Your current location is outside Sri Lanka", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            LatLng currentPos = new LatLng(lat, lng);

                            selectedLat = lat;
                            selectedLng = lng;

                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentPos)
                                    .title("My Location"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16f));

                            binding.txtSelectedLocation.setText(
                                    String.format("Selected: %.6f, %.6f", lat, lng)
                            );
                            binding.btnConfirmLocation.setEnabled(true);
                        } else {
                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSriLankaLocation(double latitude, double longitude) {
        return latitude >= SL_LAT_MIN && latitude <= SL_LAT_MAX &&
                longitude >= SL_LNG_MIN && longitude <= SL_LNG_MAX;
    }
}