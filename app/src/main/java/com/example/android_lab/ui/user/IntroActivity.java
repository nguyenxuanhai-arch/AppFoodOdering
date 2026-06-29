package com.example.android_lab.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_lab.R;
import com.example.android_lab.ui.auth.LoginActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ImageView btnGetStarted = findViewById(R.id.imageView);

        btnGetStarted.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("intro_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isFirstLaunch", false).apply();

            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            finish();
        });
    }
}
