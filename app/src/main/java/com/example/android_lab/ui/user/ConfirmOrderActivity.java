package com.example.android_lab.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_lab.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfirmOrderActivity extends AppCompatActivity {
    private EditText etName, etPhone, etAddress;
    private Button btnConfirm;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnConfirm = findViewById(R.id.btnConfirmOrder);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadUserInfo();

        btnConfirm.setOnClickListener(v -> confirmInfo());
    }

    private void loadUserInfo() {
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .get().addOnSuccessListener(this::fillUserInfo);
    }

    private void fillUserInfo(DocumentSnapshot doc) {
        etName.setText(doc.getString("name"));
        etPhone.setText(doc.getString("phone"));
        etAddress.setText(doc.getString("address"));
    }

    private void confirmInfo() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("name", name, "phone", phone, "address", address);
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("phone", phone);
        intent.putExtra("address", address);
        intent.putExtra("amount", getIntent().getDoubleExtra("amount", 0));
        intent.putExtra("cartItems", getIntent().getSerializableExtra("cartItems"));
        startActivity(intent);
        finish();
    }
}
