package com.example.android_lab.ui.admin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.example.android_lab.ui.adapter.ProductAdapter;
import com.example.android_lab.ui.admin.AddEditProductActivity;
import com.google.firebase.database.*;

import java.util.*;

public class ProductCrudFragment extends Fragment implements ProductAdapter.OnProductActionListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private RecyclerView rvProductList;

    private final List<Product> productList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private DatabaseReference productRef;

    private static final int REQ_ADD_PRODUCT = 1001;
    private static final int REQ_EDIT_PRODUCT = 1002;

    public ProductCrudFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_crud, container, false);
        initFirebase();
        bindViews(view);
        setupRecyclerView();
        setupSwipeToRefresh();
        setupAddButton(view);
        loadProductList();
        return view;
    }

    private void initFirebase() {
        productRef = FirebaseDatabase.getInstance().getReference("products");
    }

    private void bindViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        rvProductList = view.findViewById(R.id.rvMenuFood);
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(requireContext(), productList, true);
        productAdapter.setOnProductActionListener(this);
        rvProductList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProductList.setAdapter(productAdapter);
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadProductList);
    }

    private void setupAddButton(View view) {
        View fab = view.findViewById(R.id.fabAddProduct);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddEditProductActivity.class);
                startActivityForResult(intent, REQ_ADD_PRODUCT);
            });
        }
    }

    private void loadProductList() {
        progressBar.setVisibility(View.VISIBLE);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        product.setId(child.getKey());
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi: " + error.getMessage());
                hideLoading();
            }
        });
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEditProduct(Product product) {
        Intent intent = new Intent(requireContext(), AddEditProductActivity.class);
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT, product);
        startActivityForResult(intent, REQ_EDIT_PRODUCT);
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá sản phẩm")
                .setMessage("Bạn có chắc muốn xoá sản phẩm này?")
                .setPositiveButton("Xoá", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void deleteProduct(Product product) {
        productRef.child(product.getId()).removeValue()
                .addOnSuccessListener(unused -> {
                    showToast("Đã xoá sản phẩm");
                    loadProductList();
                })
                .addOnFailureListener(e -> showToast("Lỗi xoá: " + e.getMessage()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                (requestCode == REQ_ADD_PRODUCT || requestCode == REQ_EDIT_PRODUCT)) {
            loadProductList();
        }
    }

    private void showToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
