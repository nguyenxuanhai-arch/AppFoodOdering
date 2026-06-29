package com.example.android_lab.ui.user.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.example.android_lab.ui.adapter.ProductAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {
    private EditText etSearch;
    private ImageButton btnSearch;
    private ToggleButton btnFilterFood, btnFilterDrink;
    private RecyclerView rvMenu;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private DatabaseReference productsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        initViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        setupSwipeRefresh(); // <-- thêm vào
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllProducts(); // luôn load lại mỗi khi quay về
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnFilterFood = view.findViewById(R.id.btnFilterFood);
        btnFilterDrink = view.findViewById(R.id.btnFilterDrink);
        rvMenu = view.findViewById(R.id.rvMenuFood);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh); // <-- thêm vào
        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        productsRef = FirebaseDatabase.getInstance().getReference("products");
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(requireContext(), filteredProducts, false);
        rvMenu.setLayoutManager(new GridLayoutManager(requireContext(), 1));
        rvMenu.setAdapter(productAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadAllProducts);
    }

    private void loadAllProducts() {
        swipeRefreshLayout.setRefreshing(true); // Bắt đầu loading

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        allProducts.add(product);
                    }
                }
                applyFilters(); // Lọc dữ liệu theo toggle/search hiện tại
                swipeRefreshLayout.setRefreshing(false); // ✅ Dừng loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // ✅ Dừng loading nếu lỗi
            }
        });
    }

    private void setupSearchAndFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });

        btnSearch.setOnClickListener(v -> applyFilters());
        btnFilterFood.setOnClickListener(v -> applyFilters());
        btnFilterDrink.setOnClickListener(v -> applyFilters());
    }

    private void applyFilters() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        boolean filterFood = btnFilterFood.isChecked();
        boolean filterDrink = btnFilterDrink.isChecked();

        filteredProducts.clear();
        for (Product p : allProducts) {
            boolean matchesSearch = p.getName() != null && p.getName().toLowerCase().contains(query);
            boolean matchesType = (!filterFood && !filterDrink) ||
                    (filterFood && "food".equalsIgnoreCase(p.getType())) ||
                    (filterDrink && "drink".equalsIgnoreCase(p.getType()));
            if (matchesSearch && matchesType) {
                filteredProducts.add(p);
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}
