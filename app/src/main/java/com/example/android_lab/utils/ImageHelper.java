package com.example.android_lab.utils;

import android.content.Intent;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;

public class ImageHelper {

    /**
     * Mở thư viện ảnh từ ActivityResultLauncher
     */
    public static void openGallery(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }
}
