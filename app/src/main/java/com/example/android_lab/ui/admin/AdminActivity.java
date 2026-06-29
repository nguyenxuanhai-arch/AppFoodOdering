package com.example.android_lab.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.android_lab.R;
import com.example.android_lab.ui.admin.fragments.AdminPaymentFragment;
import com.example.android_lab.ui.admin.fragments.DashboardFragment;
import com.example.android_lab.ui.admin.fragments.ProductCrudFragment;
import com.example.android_lab.ui.auth.LoginActivity;
import com.example.android_lab.ui.user.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LinearLayout menuHome, menuMenu, menuPaymentHistory, menuProfile;
    private ImageView iconHome, iconMenu, iconPaymentHistory, iconProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        initViews();
        initEvents();
        initWindowInsets();

        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    private void initViews() {
        auth = FirebaseAuth.getInstance();
        menuHome = findViewById(R.id.menuHome);
        menuMenu = findViewById(R.id.menuMenu);
        menuPaymentHistory = findViewById(R.id.menuPaymentHistory);
        menuProfile = findViewById(R.id.menuProfile);
        iconHome = findViewById(R.id.iconHome);
        iconMenu = findViewById(R.id.iconMenu);
        iconPaymentHistory = findViewById(R.id.iconPaymentHistory);
        iconProfile = findViewById(R.id.iconProfile);
    }

    private void initEvents() {
        menuHome.setOnClickListener(v -> selectMenu(0));
        menuMenu.setOnClickListener(v -> selectMenu(1));
        menuPaymentHistory.setOnClickListener(v -> selectMenu(2));
        menuProfile.setOnClickListener(v -> selectMenu(3));
    }

    private void selectMenu(int index) {
        resetMenuIcons();
        switch (index) {
            case 0:
                iconHome.setImageResource(R.drawable.ic_home_selected);
                loadFragment(new DashboardFragment());
                break;
            case 1:
                iconMenu.setImageResource(R.drawable.ic_menu_selected);
                loadFragment(new ProductCrudFragment());
                break;
            case 2:
                iconPaymentHistory.setImageResource(R.drawable.ic_history_selected);
                loadFragment(new AdminPaymentFragment());
                break;
            case 3:
                iconProfile.setImageResource(R.drawable.ic_account_selected);
                loadFragment(new ProfileFragment());
                break;
        }
    }

    private void resetMenuIcons() {
        iconHome.setImageResource(R.drawable.ic_home);
        iconMenu.setImageResource(R.drawable.ic_menu);
        iconPaymentHistory.setImageResource(R.drawable.ic_history);
        iconProfile.setImageResource(R.drawable.ic_account);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void initWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
