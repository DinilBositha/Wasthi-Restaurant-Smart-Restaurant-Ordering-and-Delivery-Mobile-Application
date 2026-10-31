package com.dinilbositha.wasthirestaurant.activity;

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
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dinilbositha.wasthirestaurant.databinding.ActivityMapPinBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;

public class MapPinActivity extends AppCompatActivity {

    private ActivityMapPinBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentMarker;

    private Double selectedLat = null;
    private Double selectedLng = null;

    // Sri Lanka bounds
    private static final double SL_LAT_MIN = 5.7;
    private static final double SL_LAT_MAX = 9.9;
    private static final double SL_LNG_MIN = 79.5;
    private static final double SL_LNG_MAX = 81.9;

    // Default location - Colombo
    private static final GeoPoint DEFAULT_LOCATION = new GeoPoint(6.9271, 79.8612);

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    moveToCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied. Tap on the map to pin location.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration using internal cache to avoid permission issues
        java.io.File osmConfigPath = new java.io.File(getCacheDir(), "osmdroid");
        if (!osmConfigPath.exists()) {
            osmConfigPath.mkdirs();
        }
        Configuration.getInstance().setOsmdroidBasePath(osmConfigPath);
        java.io.File osmTileCache = new java.io.File(osmConfigPath, "tiles");
        if (!osmTileCache.exists()) {
            osmTileCache.mkdirs();
        }
        Configuration.getInstance().setOsmdroidTileCache(osmTileCache);
        Configuration.getInstance().setUserAgentValue(getPackageName());

        binding = ActivityMapPinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupMap();
        setupClicks();
    }

    private void setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(9.0);
        binding.mapView.getController().setCenter(DEFAULT_LOCATION);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (p != null) {
                    onLocationSelected(p.getLatitude(), p.getLongitude(), false);
                }
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mReceive);
        binding.mapView.getOverlays().add(0, mapEventsOverlay);

        // Check if initial lat/lng passed
        double initLat = getIntent().getDoubleExtra("init_lat", 0);
        double initLng = getIntent().getDoubleExtra("init_lng", 0);

        if (initLat != 0 && initLng != 0) {
            onLocationSelected(initLat, initLng, true);
        }
    }

    private void onLocationSelected(double lat, double lng, boolean zoomIn) {
        if (!isSriLankaLocation(lat, lng)) {
            Toast.makeText(this, "Please select a location within Sri Lanka", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedLat = lat;
        selectedLng = lng;

        GeoPoint point = new GeoPoint(lat, lng);

        if (currentMarker == null) {
            currentMarker = new Marker(binding.mapView);
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            currentMarker.setTitle("Delivery Location");
            binding.mapView.getOverlays().add(currentMarker);
        }

        currentMarker.setPosition(point);

        if (zoomIn) {
            binding.mapView.getController().setZoom(16.0);
            binding.mapView.getController().setCenter(point);
        } else {
            binding.mapView.getController().animateTo(point);
        }

        binding.mapView.invalidate();

        updateAddressPreview(lat, lng);
        binding.btnConfirmLocation.setEnabled(true);
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
            boolean hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (hasFine || hasCoarse) {
                moveToCurrentLocation();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });
    }

    private void moveToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!isGpsEnabled) {
            Toast.makeText(this, "Please enable GPS / Location on your device", Toast.LENGTH_SHORT).show();
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            handleMapLocation(location);
                        } else {
                            try {
                                fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                                    if (lastLoc != null) {
                                        handleMapLocation(lastLoc);
                                    } else {
                                        Toast.makeText(this, "Could not detect GPS fix. Tap directly on the map to pin your location.", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "GPS unavailable. Tap on the map to pin location.", Toast.LENGTH_SHORT).show();
                                });
                            } catch (SecurityException ignored) {
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        try {
                            fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                                if (lastLoc != null) {
                                    handleMapLocation(lastLoc);
                                } else {
                                    Toast.makeText(this, "Location service unavailable. Tap on the map to pin location.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (SecurityException ignored) {
                        }
                    });
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMapLocation(Location location) {
        if (location == null || isFinishing()) return;
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (!isSriLankaLocation(lat, lng)) {
            Toast.makeText(this, "GPS location is outside Sri Lanka. Showing default location on map.", Toast.LENGTH_LONG).show();
            onLocationSelected(DEFAULT_LOCATION.getLatitude(), DEFAULT_LOCATION.getLongitude(), true);
            return;
        }

        onLocationSelected(lat, lng, true);
    }

    private void updateAddressPreview(double lat, double lng) {
        binding.txtSelectedLocation.setText(
                String.format(Locale.getDefault(), "Selected: %.6f, %.6f", lat, lng)
        );

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1, addresses -> {
                    if (addresses != null && !addresses.isEmpty() && !isFinishing()) {
                        Address addr = addresses.get(0);
                        String line = addr.getAddressLine(0);
                        if (!TextUtils.isEmpty(line)) {
                            runOnUiThread(() -> binding.txtSelectedLocation.setText(line));
                        }
                    }
                });
            } else {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String line = addresses.get(0).getAddressLine(0);
                    if (!TextUtils.isEmpty(line)) {
                        binding.txtSelectedLocation.setText(line);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isSriLankaLocation(double latitude, double longitude) {
        return latitude >= SL_LAT_MIN && latitude <= SL_LAT_MAX &&
                longitude >= SL_LNG_MIN && longitude <= SL_LNG_MAX;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onPause();
        }
    }
}