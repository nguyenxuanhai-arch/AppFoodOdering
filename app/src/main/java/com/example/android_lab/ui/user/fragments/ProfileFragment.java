package com.example.android_lab.ui.user.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.android_lab.R;
import com.example.android_lab.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private LinearLayout layoutName, layoutAddress, layoutEmail, layoutPhone, layoutPassword;
    private TextView btnLogout;
    private TextView btnUpdateProfile;
    private TextView btnChangePassword;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isFirstLoad = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        setupLogoutButton();
        setupUpdateProfileButton();
        setupChangePasswordButton();
        setupSwipeToRefresh();
        loadUserProfile();
        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        layoutName = view.findViewById(R.id.layoutName);
        layoutAddress = view.findViewById(R.id.layoutAddress);
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutPhone = view.findViewById(R.id.layoutPhone);
        layoutPassword = view.findViewById(R.id.layoutPassword);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Gán label thủ công
        setLabel(layoutName, "Họ tên:");
        setLabel(layoutAddress, "Địa chỉ:");
        setLabel(layoutEmail, "Email:");
        setLabel(layoutPhone, "Số điện thoại:");
        setLabel(layoutPassword, "Mật khẩu:");
    }

    private void setLabel(LinearLayout layout, String labelText) {
        TextView label = layout.findViewById(R.id.label);
        label.setText(labelText);
    }


    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupUpdateProfileButton() {
        btnUpdateProfile.setOnClickListener(v -> showUpdateProfileDialog());
    }

    private void setupChangePasswordButton() {
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUserProfile);
    }

    private void loadUserProfile() {
        swipeRefreshLayout.setRefreshing(true);
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            swipeRefreshLayout.setRefreshing(false);
            showLoginRequired();
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setValue(layoutName, documentSnapshot.getString("name"));
                        setValue(layoutAddress, documentSnapshot.getString("address"));
                        setValue(layoutEmail, documentSnapshot.getString("email"));
                        setValue(layoutPhone, documentSnapshot.getString("phone"));
                        setValue(layoutPassword, "************");
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Lỗi tải hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void logout() {
        auth.signOut();
        startActivity(new Intent(requireActivity(), LoginActivity.class));
        requireActivity().finish();
    }

    private void setLabelFromTag(LinearLayout layout) {
        TextView label = layout.findViewById(R.id.label);
        if (layout.getTag() != null) {
            label.setText(layout.getTag().toString());
        }
    }

    private void setValue(LinearLayout layout, String value) {
        TextView valueText = layout.findViewById(R.id.value);
        valueText.setText(value != null ? value : "Không có");
    }

    private void showUpdateProfileDialog() {
        // Hiển thị dialog cập nhật thông tin cá nhân
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Cập nhật thông tin cá nhân");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_profile, null);
        builder.setView(dialogView);

        final android.widget.EditText edtName = dialogView.findViewById(R.id.edtName);
        final android.widget.EditText edtAddress = dialogView.findViewById(R.id.edtAddress);
        final android.widget.EditText edtPhone = dialogView.findViewById(R.id.edtPhone);

        // Lấy giá trị hiện tại
        edtName.setText(((TextView) layoutName.findViewById(R.id.value)).getText());
        edtAddress.setText(((TextView) layoutAddress.findViewById(R.id.value)).getText());
        edtPhone.setText(((TextView) layoutPhone.findViewById(R.id.value)).getText());

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            updateUserProfile(name, address, phone);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateUserProfile(String name, String address, String phone) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        db.collection("users").document(userId)
                .update("name", name, "address", address, "phone", phone)
                .addOnSuccessListener(unused -> {
                    android.widget.Toast.makeText(getContext(), "Cập nhật thành công", android.widget.Toast.LENGTH_SHORT).show();
                    loadUserProfile();
                })
                .addOnFailureListener(e -> android.widget.Toast.makeText(getContext(), "Cập nhật thất bại", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        showChangePasswordDialog(auth.getCurrentUser());
    }

    // Overload: nhận user làm tham số
    private void showChangePasswordDialog(FirebaseUser user) {
        boolean isGoogleAccount = false;
        if (user != null && user.getProviderData() != null) {
            for (com.google.firebase.auth.UserInfo info : user.getProviderData()) {
                if ("google.com".equals(info.getProviderId())) {
                    isGoogleAccount = true;
                    break;
                }
            }
        }
        if (isGoogleAccount) {
            showSetPasswordForGoogleDialog(user);
            return;
        }
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Đổi mật khẩu");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final android.widget.EditText edtCurrent = dialogView.findViewById(R.id.edtCurrentPassword);
        final android.widget.EditText edtNew = dialogView.findViewById(R.id.edtNewPassword);
        final android.widget.EditText edtConfirm = dialogView.findViewById(R.id.edtConfirmPassword);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String current = edtCurrent.getText().toString();
            String newPass = edtNew.getText().toString();
            String confirm = edtConfirm.getText().toString();
            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)) {
                android.widget.Toast.makeText(getContext(), "Mật khẩu mới không khớp", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            changePassword(current, newPass);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showSetPasswordForGoogleDialog(FirebaseUser user) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Thiết lập mật khẩu cho tài khoản Google");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_set_password_google, null);
        builder.setView(dialogView);

        final android.widget.EditText edtNew = dialogView.findViewById(R.id.edtNewPasswordForGoogle);
        final android.widget.EditText edtConfirm = dialogView.findViewById(R.id.edtConfirmPasswordForGoogle);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newPass = edtNew.getText().toString();
            String confirm = edtConfirm.getText().toString();
            if (newPass.isEmpty() || confirm.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirm)) {
                android.widget.Toast.makeText(getContext(), "Mật khẩu mới không khớp", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            linkPasswordForGoogleAccount(user, newPass);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void linkPasswordForGoogleAccount(FirebaseUser user, String newPassword) {
        if (user == null || user.getEmail() == null) return;
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), newPassword);
        user.linkWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                android.widget.Toast.makeText(getContext(), "Đã thêm mật khẩu cho tài khoản Google!", android.widget.Toast.LENGTH_SHORT).show();
                // Reload user để cập nhật provider
                user.reload().addOnCompleteListener(task -> {
                    FirebaseUser refreshedUser = FirebaseAuth.getInstance().getCurrentUser();
                    new android.os.Handler().postDelayed(() -> showChangePasswordDialog(refreshedUser), 300);
                });
            })
            .addOnFailureListener(e -> {
                String msg = e.getMessage();
                if (msg != null && msg.contains("already linked")) {
                    android.widget.Toast.makeText(getContext(), "Tài khoản đã có mật khẩu, hãy sử dụng chức năng đổi mật khẩu thông thường.", android.widget.Toast.LENGTH_LONG).show();
                } else {
                    android.widget.Toast.makeText(getContext(), "Không thể thêm mật khẩu: " + msg, android.widget.Toast.LENGTH_LONG).show();
                }
            });
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;
        // Re-authenticate
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
            .addOnSuccessListener(unused -> {
                user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> android.widget.Toast.makeText(getContext(), "Đổi mật khẩu thành công", android.widget.Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> android.widget.Toast.makeText(getContext(), "Đổi mật khẩu thất bại: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> android.widget.Toast.makeText(getContext(), "Mật khẩu hiện tại không đúng", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void showLoginRequired() {
        Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(requireActivity(), LoginActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFirstLoad) {
            loadUserProfile();
        } else {
            isFirstLoad = false;
        }
    }
}
