package com.dinilbositha.wasthirestaurantadmin.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";

    public interface UploadListener {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
        void onProgress(long bytes, long totalBytes);
    }

    public static void uploadImage(Context context, Uri imageUri, String folderName, UploadListener listener) {
        if (imageUri == null) {
            if (listener != null) listener.onError("Image Uri is null");
            return;
        }

        File tempFile = null;
        try {
            String uploadPath = null;
            if (context != null) {
                tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + ".jpg");
                try (InputStream is = context.getContentResolver().openInputStream(imageUri);
                     FileOutputStream fos = new FileOutputStream(tempFile)) {
                    if (is != null) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        fos.flush();
                        uploadPath = tempFile.getAbsolutePath();
                    }
                }
            }

            final File fileToDelete = tempFile;

            UploadCallback callback = new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    Log.d(TAG, "Upload started: " + requestId);
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    if (listener != null) {
                        listener.onProgress(bytes, totalBytes);
                    }
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    if (fileToDelete != null && fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                    String secureUrl = (String) resultData.get("secure_url");
                    Log.d(TAG, "Upload success: " + secureUrl);
                    if (listener != null) {
                        listener.onSuccess(secureUrl);
                    }
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    if (fileToDelete != null && fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                    String errorMsg = error != null ? error.getDescription() : "Unknown error";
                    Log.e(TAG, "Upload error: " + errorMsg);
                    if (listener != null) {
                        listener.onError(errorMsg);
                    }
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                }
            };

            if (uploadPath != null) {
                MediaManager.get().upload(uploadPath)
                        .unsigned("wasthi_restaurant")
                        .option("folder", folderName)
                        .callback(callback)
                        .dispatch();
            } else {
                MediaManager.get().upload(imageUri)
                        .unsigned("wasthi_restaurant")
                        .option("folder", folderName)
                        .callback(callback)
                        .dispatch();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception preparing image for upload: " + e.getMessage(), e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (listener != null) {
                listener.onError(e.getMessage() != null ? e.getMessage() : "Failed to process image");
            }
        }
    }

    public static void uploadImage(Uri imageUri, String folderName, UploadListener listener) {
        uploadImage(null, imageUri, folderName, listener);
    }
}
