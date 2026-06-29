package com.example.android_lab.ui.user.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.example.android_lab.ui.adapter.BannerAdapter;
import com.example.android_lab.ui.adapter.PopularProductAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewPager2 bannerViewPager;
    private RecyclerView rvPopularProduct;
    private ProgressBar progressBar;
    private PopularProductAdapter popularProductAdapter;
    private DatabaseReference productRef;
    private Handler handler;
    private Runnable bannerRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadPopularProducts);

        initializeViews(view);
        setupBanner();
        setupRecyclerView();
        loadPopularProducts();
        return view;
    }

    private void initializeViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        rvPopularProduct = view.findViewById(R.id.rvPopularProduct); // ID layout cũ có thể giữ nguyên
        progressBar = view.findViewById(R.id.progressBar);

        productRef = FirebaseDatabase.getInstance().getReference("products");
        handler = new Handler(Looper.getMainLooper());
    }

    private void setupBanner() {
        List<Integer> bannerImages = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        );

        BannerAdapter bannerAdapter = new BannerAdapter(requireContext(), bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);
        bannerViewPager.setOffscreenPageLimit(3);

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isDetached()) {
                    int currentItem = bannerViewPager.getCurrentItem();
                    int totalItems = bannerAdapter.getItemCount();
                    int nextItem = (currentItem + 1) % totalItems;
                    bannerViewPager.setCurrentItem(nextItem, true);
                    handler.postDelayed(this, 3000);
                }
            }
        };
    }

    private void setupRecyclerView() {
        popularProductAdapter = new PopularProductAdapter(requireContext());
        rvPopularProduct.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        rvPopularProduct.setAdapter(popularProductAdapter);
    }

    private void loadPopularProducts() {
        if (!isAdded()) return;
        progressBar.setVisibility(View.VISIBLE);

        Query query = productRef.orderByChild("popular").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> productList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Product product = child.getValue(Product.class);
                    if (product != null) {
                        product.setId(child.getKey());
                        productList.add(product);
                    }
                }
                popularProductAdapter.updateData(productList);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(bannerRunnable, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(bannerRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(bannerRunnable);
    }
}
