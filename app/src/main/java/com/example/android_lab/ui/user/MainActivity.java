package com.example.android_lab.ui.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import com.example.android_lab.R;
import com.example.android_lab.ui.auth.LoginActivity;
import com.example.android_lab.ui.user.fragments.CartFragment;
import com.example.android_lab.ui.user.fragments.HomeFragment;
import com.example.android_lab.ui.user.fragments.PaymentHistoryFragment;
import com.example.android_lab.ui.user.fragments.ProfileFragment;
import com.example.android_lab.ui.user.fragments.MenuFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private LinearLayout menuHome, menuMenu, menuProfile, menuCart, menuPaymentHistory;
    private ImageView iconHome, iconMenu, iconProfile, iconCart, iconPaymentHistory;
    private HomeFragment homeFragment;
    private MenuFragment menuFragment;
    private ProfileFragment profileFragment;
    private CartFragment cartFragment;
    private PaymentHistoryFragment paymentHistoryFragment;

    private int currentMenuIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("intro_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            startActivity(new Intent(this, IntroActivity.class));
            prefs.edit().putBoolean("isFirstLaunch", false).apply();
            finish();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        initFragments();
        setupMenuClickListeners();
// ✅ Thay load mặc định bằng xử lý điều hướng theo Intent
        String navigateTo = getIntent().getStringExtra("navigateTo");

        switch (navigateTo != null ? navigateTo : "") {
            case "cart":
                updateMenuIcons(2);
                loadFragment(cartFragment, 2);
                break;
            case "paymentHistory":
                updateMenuIcons(3);
                loadFragment(paymentHistoryFragment, 3);
                break;
            case "profile":
                updateMenuIcons(4);
                loadFragment(profileFragment, 4);
                break;
            default:
                updateMenuIcons(0);
                loadFragment(homeFragment, 0);
                break;
        }

        if (savedInstanceState != null) {
            currentMenuIndex = savedInstanceState.getInt("currentMenuIndex", 0);
        }

        // Thêm xử lý back button mới
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                    // Cập nhật currentMenuIndex dựa trên fragment hiện tại
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (currentFragment instanceof HomeFragment) {
                        currentMenuIndex = 0;
                    } else if (currentFragment instanceof MenuFragment) {
                        currentMenuIndex = 1;
                    } else if (currentFragment instanceof CartFragment) {
                        currentMenuIndex = 2;
                    } else if (currentFragment instanceof PaymentHistoryFragment) {
                        currentMenuIndex = 3;
                    } else if (currentFragment instanceof ProfileFragment) {
                        currentMenuIndex = 4;
                    }
                    updateMenuIcons(currentMenuIndex);
                } else {
                    setEnabled(false);
                    MainActivity.this.getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initViews() {
        menuHome = findViewById(R.id.menuHome);
        menuMenu = findViewById(R.id.menuMenu);
        menuProfile = findViewById(R.id.menuProfile);
        menuCart = findViewById(R.id.menuCart);
        menuPaymentHistory = findViewById(R.id.menuPaymentHistory);

        iconHome = findViewById(R.id.iconHome);
        iconMenu = findViewById(R.id.iconMenu);
        iconProfile = findViewById(R.id.iconProfile);
        iconCart = findViewById(R.id.iconCart);
        iconPaymentHistory = findViewById(R.id.iconPaymentHistory);
    }

    private void initFragments() {
        if (homeFragment == null) homeFragment = new HomeFragment();
        if (menuFragment == null) menuFragment = new MenuFragment();
        if (cartFragment == null) cartFragment = new CartFragment();
        if (profileFragment == null) profileFragment = new ProfileFragment();
        if (paymentHistoryFragment == null) paymentHistoryFragment = new PaymentHistoryFragment();
    }

    private void setupMenuClickListeners() {
        menuHome.setOnClickListener(v -> {
            if (currentMenuIndex != 0) {
                updateMenuIcons(0);
                loadFragment(homeFragment, 0);
            }
        });
        menuMenu.setOnClickListener(v -> {
            if (currentMenuIndex != 1) {
                updateMenuIcons(1);
                loadFragment(menuFragment, 1);
            }
        });
        menuCart.setOnClickListener(v -> {
            if (currentMenuIndex != 2) {
                updateMenuIcons(2);
                loadFragment(cartFragment, 2);
            }
        });
        menuPaymentHistory.setOnClickListener(v -> {
            if (currentMenuIndex != 3) {
                updateMenuIcons(3);
                loadFragment(paymentHistoryFragment, 3);
            }
        });
        menuProfile.setOnClickListener(v -> {
            if (currentMenuIndex != 4) {
                updateMenuIcons(4);
                loadFragment(profileFragment, 4);
            }
        });
    }

    private void updateMenuIcons(int selectedIndex) {
        // Reset tất cả
        iconHome.setImageResource(R.drawable.ic_home);
        iconMenu.setImageResource(R.drawable.ic_menu);
        iconProfile.setImageResource(R.drawable.ic_account);
        iconCart.setImageResource(R.drawable.ic_cart);
        iconPaymentHistory.setImageResource(R.drawable.ic_history);


        switch (selectedIndex) {
            case 0:
                iconHome.setImageResource(R.drawable.ic_home_selected);
                break;
            case 1:
                iconMenu.setImageResource(R.drawable.ic_menu_selected);
                break;
            case 2:
                iconCart.setImageResource(R.drawable.ic_cart_selected);
                break;
            case 3:
                iconPaymentHistory.setImageResource(R.drawable.ic_history_selected);
                break;
            case 4:
                iconProfile.setImageResource(R.drawable.ic_account_selected);
                break;
        }

        currentMenuIndex = selectedIndex;
    }


    private void loadFragment(Fragment fragment, int menuIndex) {
        if (fragment == null) return;

        // Kiểm tra xem fragment hiện tại có đang được hiển thị không
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        // Xác định animation dựa vào hướng chuyển fragment
        int enterAnim = menuIndex > currentMenuIndex ? R.anim.slide_in_right : R.anim.slide_in_left;
        int exitAnim = menuIndex > currentMenuIndex ? R.anim.slide_out_left : R.anim.slide_out_right;

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(enterAnim, exitAnim, enterAnim, exitAnim)
                .replace(R.id.fragmentContainer, fragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)  // Thêm vào back stack để xử lý back button
                .commit();

        currentMenuIndex = menuIndex;
        updateMenuIcons(menuIndex);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentMenuIndex", currentMenuIndex);
    }
}
