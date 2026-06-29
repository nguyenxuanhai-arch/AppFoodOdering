package com.example.android_lab.ui.user.fragments;

import android.content.Intent;
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
import com.example.android_lab.models.CartItem;
import com.example.android_lab.models.Product;
import com.example.android_lab.ui.adapter.CartAdapter;
import com.example.android_lab.ui.user.ConfirmOrderActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CartFragment extends Fragment {

    private CartAdapter adapter;
    private final List<CartItem> cartList = new ArrayList<>();
    private TextView tvTotal, btnProceed;

    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    public CartFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        RecyclerView rvCart = view.findViewById(R.id.rvCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnProceed = view.findViewById(R.id.btnProceed);

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartList, this::removeItem, this::updateQuantity);
        rvCart.setAdapter(adapter);

        setupListeners();
        return view;
    }

    private void setupListeners() {
        btnProceed.setOnClickListener(v -> {
            double total = calculateTotal();
            Intent intent = new Intent(getContext(), ConfirmOrderActivity.class);
            intent.putExtra("amount", total);
            intent.putExtra("cartItems", new ArrayList<>(cartList));
            startActivity(intent);
        });
    }

    private void loadCartDataFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("cart").child(uid);

        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                cartList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String type = child.child("type").getValue(String.class);
                    if ("product".equals(type)) {
                        Product product = child.getValue(Product.class);
                        if (product != null) cartList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                calculateTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        };

        cartRef.addValueEventListener(cartListener);
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotal.setText(String.format("%,.0f₫", total));
        return total;
    }

    private void removeItem(CartItem item) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("cart").child(uid).child(item.getId()).removeValue();
    }

    private void updateQuantity(CartItem item, int newQty) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cart").child(uid).child(item.getId());

        ref.child("quantity").setValue(newQty)
                .addOnSuccessListener(unused -> calculateTotal())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onStart() {
        super.onStart();
        loadCartDataFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
    }
}
