package com.example.android_lab.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_lab.R;
import com.example.android_lab.models.PaymentHistoryItem;
import com.example.android_lab.ui.adapter.PaymentHistoryAdapter;
import com.example.android_lab.utils.StatusMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminPaymentFragment extends Fragment {
    private RecyclerView rvPayments;
    private ProgressBar progressBar;
    private PaymentHistoryAdapter adapter;
    private final List<PaymentHistoryItem> paymentList = new ArrayList<>();
    private DatabaseReference paymentRef;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_payment, container, false);

        rvPayments = view.findViewById(R.id.rvPayments);
        progressBar = view.findViewById(R.id.progressBar);

        adapter = new PaymentHistoryAdapter(paymentList);
        adapter.setAdmin(true); // Bật chế độ admin
        adapter.setOnStatusUpdateListener((item, newStatusVi) -> updatePaymentStatus(item, newStatusVi));

        rvPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPayments.setAdapter(adapter);

        paymentRef = FirebaseDatabase.getInstance().getReference("payments");
        db = FirebaseFirestore.getInstance();

        loadPayments();
        return view;
    }

    private void loadPayments() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && "admin".equals(documentSnapshot.getString("role"))) {
                        loadAllPayments();
                    } else {
                        Toast.makeText(getContext(), "Bạn không có quyền truy cập phần này", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void loadAllPayments() {
        paymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentList.clear();
                try {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        for (DataSnapshot paymentSnap : userSnap.getChildren()) {
                            PaymentHistoryItem item = paymentSnap.getValue(PaymentHistoryItem.class);
                            if (item != null) {
                                item.setUserId(userSnap.getKey());
                                paymentList.add(item);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updatePaymentStatus(PaymentHistoryItem item, String newStatusVi) {
        if (item.getUserId() == null) {
            Toast.makeText(getContext(), "Thiếu thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatusEn = StatusMapper.toEnglish(newStatusVi);

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference paymentItemRef = paymentRef
                .child(item.getUserId())
                .child(item.getId());

        paymentItemRef.child("status").setValue(newStatusEn)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    item.setStatus(newStatusEn); // lưu dạng tiếng Anh
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }
}
