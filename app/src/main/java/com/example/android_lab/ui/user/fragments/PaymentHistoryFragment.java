package com.example.android_lab.ui.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_lab.R;
import com.example.android_lab.models.PaymentHistoryItem;
import com.example.android_lab.ui.adapter.PaymentHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaymentHistoryFragment extends Fragment {
    private RecyclerView rvHistory;
    private TextView tvEmptyState;
    private PaymentHistoryAdapter adapter;
    private final List<PaymentHistoryItem> historyList = new ArrayList<>();
    private ValueEventListener paymentListener;
    private DatabaseReference paymentRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_history, container, false);
        rvHistory = view.findViewById(R.id.rvPaymentHistory);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        adapter = new PaymentHistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> {
            if (isAdded() && getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), com.example.android_lab.ui.user.OrderDetailActivity.class);
                intent.putExtra("order", item);
                startActivity(intent);
            }
        });
        loadPaymentHistory();
        return view;
    }

    private void loadPaymentHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        paymentRef = FirebaseDatabase.getInstance().getReference("payments").child(uid);

        paymentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Kiểm tra fragment còn được attach không

                historyList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PaymentHistoryItem item = child.getValue(PaymentHistoryItem.class);
                    if (item != null) historyList.add(item);
                }
                adapter.notifyDataSetChanged();
                if (tvEmptyState != null) {
                    tvEmptyState.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải lịch sử thanh toán", Toast.LENGTH_SHORT).show();
                }
            }
        };

        paymentRef.addValueEventListener(paymentListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (paymentRef != null && paymentListener != null) {
            paymentRef.removeEventListener(paymentListener);
        }
        tvEmptyState = null;
        rvHistory = null;
    }
}
