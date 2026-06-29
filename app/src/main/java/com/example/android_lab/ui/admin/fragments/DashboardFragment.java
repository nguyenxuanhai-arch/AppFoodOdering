package com.example.android_lab.ui.admin.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.android_lab.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.*;

import java.util.*;

public class DashboardFragment extends Fragment {

    private TextView tvTotalRevenue, tvTodayOrders, tvTotalProducts;
    private TextView tvPendingOrders, tvProcessingOrders, tvCompletedOrders, tvCancelledOrders;
    private PieChart orderStatusChart;

    private DatabaseReference paymentsRef, productsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Bind View
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvTodayOrders = view.findViewById(R.id.tvTodayOrders);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvPendingOrders = view.findViewById(R.id.tvPendingOrders);
        tvProcessingOrders = view.findViewById(R.id.tvProcessingOrders);
        tvCompletedOrders = view.findViewById(R.id.tvCompletedOrders);
        tvCancelledOrders = view.findViewById(R.id.tvCancelledOrders);
        orderStatusChart = view.findViewById(R.id.orderStatusChart);

        // Firebase
        paymentsRef = FirebaseDatabase.getInstance().getReference("payments");
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        loadProductCount();
        loadPaymentStats();

        return view;
    }

    private void loadProductCount() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                int count = (int) snapshot.getChildrenCount();
                tvTotalProducts.setText("Tổng số sản phẩm: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi tải số lượng sản phẩm");
            }
        });
    }

    private void loadPaymentStats() {
        if (!isAdded()) return;
        long now = System.currentTimeMillis();
        long startOfToday = getStartOfDay(now);

        final int[] revenueToday = {0};
        final int[] totalTodayOrders = {0};
        final int[] pending = {0};
        final int[] processing = { 0 };
        final int[] completed = { 0 };
        final int[] cancelled = { 0 };

        paymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    for (DataSnapshot paymentSnap : userSnap.getChildren()) {
                        Map<String, Object> data = (Map<String, Object>) paymentSnap.getValue();
                        if (data == null) continue;

                        Long timestamp = safeLong(data.get("timestamp"));
                        Integer amount = safeInt(data.get("amount"));
                        String status = (String) data.get("status");

                        if (timestamp == null || amount == null || status == null) continue;

                        if (timestamp >= startOfToday) {
                            revenueToday[0] += amount;
                            totalTodayOrders[0]++;
                        }

                        switch (status.toLowerCase()) {
                            case "pending":     pending[0]++; break;
                            case "processing":  processing[0]++; break;
                            case "completed":   completed[0]++; break;
                            case "cancelled":   cancelled[0]++; break;
                        }
                    }
                }

                if (!isAdded()) return;

                tvTotalRevenue.setText("Tổng doanh thu hôm nay: " + formatCurrency(revenueToday[0]));
                tvTodayOrders.setText("Đơn hàng hôm nay: " + totalTodayOrders[0]);
                tvPendingOrders.setText("Chờ xác nhận: " + pending[0]);
                tvProcessingOrders.setText("Đang xử lý: " + processing[0]);
                tvCompletedOrders.setText("Đã hoàn thành: " + completed[0]);
                tvCancelledOrders.setText("Đã hủy: " + cancelled[0]);

                setupPieChart(pending[0], processing[0], completed[0], cancelled[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi tải dữ liệu đơn hàng");
            }
        });
    }

    private void setupPieChart(int pending, int processing, int completed, int cancelled) {
        List<PieEntry> entries = new ArrayList<>();
        if (pending > 0) entries.add(new PieEntry(pending, "Chờ duyệt"));
        if (processing > 0) entries.add(new PieEntry(processing, "Đang xử lý"));
        if (completed > 0) entries.add(new PieEntry(completed, "Hoàn thành"));
        if (cancelled > 0) entries.add(new PieEntry(cancelled, "Đã hủy"));

        PieDataSet dataSet = new PieDataSet(entries, "Trạng thái đơn hàng");
        dataSet.setColors(Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED);
        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.BLACK);

        orderStatusChart.setData(pieData);
        orderStatusChart.setUsePercentValues(true);
        orderStatusChart.setCenterText("Thống kê đơn hàng");
        orderStatusChart.setCenterTextSize(16f);

        Description desc = new Description();
        desc.setText("");
        orderStatusChart.setDescription(desc);
        orderStatusChart.invalidate();
    }

    private long getStartOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String formatCurrency(int amount) {
        return String.format(Locale.getDefault(), "%,d₫", amount);
    }

    private Long safeLong(Object obj) {
        try {
            return obj instanceof Number ? ((Number) obj).longValue() : Long.parseLong(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer safeInt(Object obj) {
        try {
            return obj instanceof Number ? ((Number) obj).intValue() : Integer.parseInt(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void showToast(String message) {
        if (getContext() != null)
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
