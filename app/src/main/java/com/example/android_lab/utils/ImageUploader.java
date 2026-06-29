package com.example.android_lab.utils;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageUploader {

    /**
     * Callback để xử lý kết quả upload ảnh
     */
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }
    public static void uploadImageProduct(@NonNull Uri imageUri, @NonNull String drinkId,
                                       @NonNull UploadCallback callback) {

        if (imageUri.toString().isEmpty() || drinkId.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Image URI hoặc Product ID không hợp lệ"));
            return;
        }

        // Tạo đường dẫn lưu trữ trong Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("products/" + drinkId + ".jpg");

        // Bắt đầu upload file ảnh
        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Lấy URL sau khi upload thành công
            storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }
}
