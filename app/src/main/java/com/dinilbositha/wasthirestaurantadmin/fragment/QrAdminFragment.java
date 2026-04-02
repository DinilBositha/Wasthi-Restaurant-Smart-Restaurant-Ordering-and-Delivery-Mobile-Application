package com.dinilbositha.wasthirestaurantadmin.fragment;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dinilbositha.wasthirestaurantadmin.model.TableModel;
import com.dinilbositha.wasthirestaurantadmin.databinding.FragmentQrAdminBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.OutputStream;
import java.util.Locale;

public class QrAdminFragment extends Fragment {

    private FragmentQrAdminBinding binding;
    private Bitmap qrBitmap;

    private static final String RESTAURANT_NAME = "WASTHI";
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQrAdminBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnGenerateQr.setOnClickListener(v -> generateQrCodeAndSaveToDb());
        binding.btnDownloadQr.setOnClickListener(v -> downloadQrCode());
        binding.btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void generateQrCodeAndSaveToDb() {
        String tableInput = binding.etTableNumber.getText() != null
                ? binding.etTableNumber.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(tableInput)) {
            Toast.makeText(requireContext(), "Enter table number", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedTableNo = formatTableNumber(tableInput);
        String qrText = RESTAURANT_NAME + "_TABLE_" + formattedTableNo;

        // duplicate check + save
        firestore.collection("tables")
                .document(formattedTableNo)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(requireContext(), "Table number already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        TableModel tableModel = TableModel.builder()
                                .tableNumber(formattedTableNo)
                                .qrValue(qrText)
                                .active(true)
                                .build();

                        firestore.collection("tables")
                                .document(formattedTableNo)
                                .set(tableModel)
                                .addOnSuccessListener(unused -> {
                                    try {
                                        qrBitmap = createQrBitmap(qrText, 900, 900);
                                        binding.imgQrPreview.setImageBitmap(qrBitmap);
                                        binding.tvQrText.setText(qrText);

                                        Toast.makeText(requireContext(), "QR generated and table saved", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(requireContext(), "Failed to generate QR", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(),
                                                "Failed to save table: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to check table: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void downloadQrCode() {
        if (qrBitmap == null) {
            Toast.makeText(requireContext(), "Generate QR first", Toast.LENGTH_SHORT).show();
            return;
        }

        String tableInput = binding.etTableNumber.getText() != null
                ? binding.etTableNumber.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(tableInput)) {
            Toast.makeText(requireContext(), "Invalid table number", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedTableNo = formatTableNumber(tableInput);
        String fileName = RESTAURANT_NAME + "_TABLE_" + formattedTableNo + ".png";

        saveBitmapToGallery(qrBitmap, fileName);
    }

    private String formatTableNumber(String tableInput) {
        try {
            int number = Integer.parseInt(tableInput);
            return String.format(Locale.getDefault(), "%02d", number);
        } catch (Exception e) {
            return tableInput;
        }
    }

    private Bitmap createQrBitmap(String text, int width, int height) throws Exception {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }

    private void saveBitmapToGallery(Bitmap bitmap, String fileName) {
        OutputStream outputStream = null;

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/WasthiQR");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri uri = requireContext().getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            );

            if (uri == null) {
                Toast.makeText(requireContext(), "Failed to create file", Toast.LENGTH_SHORT).show();
                return;
            }

            outputStream = requireContext().getContentResolver().openOutputStream(uri);

            if (outputStream == null) {
                Toast.makeText(requireContext(), "Failed to open output stream", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            if (!saved) {
                Toast.makeText(requireContext(), "Failed to save QR", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues updateValues = new ContentValues();
                updateValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                requireContext().getContentResolver().update(uri, updateValues, null, null);
            }

            Toast.makeText(requireContext(), "QR downloaded successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving QR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}