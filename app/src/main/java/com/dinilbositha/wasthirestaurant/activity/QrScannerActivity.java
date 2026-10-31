package com.dinilbositha.wasthirestaurant.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.dinilbositha.wasthirestaurant.databinding.ActivityQrScannerBinding;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import java.util.Collections;
import java.util.List;

public class QrScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;
    private boolean scanned = false;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startScanner();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUI();
        checkCameraPermission();
    }

    private void setupUI() {
        binding.btnCloseScanner.setOnClickListener(v -> finish());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanner() {
        binding.barcodeScanner.getBarcodeView().setDecoderFactory(
                new com.journeyapps.barcodescanner.DefaultDecoderFactory(
                        Collections.singletonList(BarcodeFormat.QR_CODE)
                )
        );

        binding.barcodeScanner.initializeFromIntent(getIntent());
        binding.barcodeScanner.decodeContinuous(callback);
        binding.barcodeScanner.resume();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null || scanned) return;

            scanned = true;

            String scannedText = result.getText().trim();

            Intent data = new Intent();
            data.putExtra("scanned_qr", scannedText);
            setResult(RESULT_OK, data);
            finish();
        }

        @Override
        public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null) {
            binding.barcodeScanner.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (binding != null) {
            binding.barcodeScanner.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}