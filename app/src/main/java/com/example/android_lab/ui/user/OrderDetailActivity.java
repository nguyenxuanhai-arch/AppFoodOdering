package com.example.android_lab.ui.user;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_lab.R;
import com.example.android_lab.models.PaymentHistoryItem;
import com.example.android_lab.utils.StatusMapper; // ✅ import thêm

public class OrderDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        TextView tvOrderId = findViewById(R.id.tvOrderId);
        TextView tvOrderStatus = findViewById(R.id.tvOrderStatus);
        TextView tvOrderDate = findViewById(R.id.tvOrderDate);
        TextView tvOrderTotal = findViewById(R.id.tvOrderTotal);
        TextView tvOrderContact = findViewById(R.id.tvOrderContact);
        TextView tvOrderProducts = findViewById(R.id.tvOrderProducts);

        PaymentHistoryItem item = (PaymentHistoryItem) getIntent().getSerializableExtra("order");
        if (item != null) {
            tvOrderId.setText(item.getOrderId());

            String statusVi = StatusMapper.toVietnamese(item.getStatus());
            tvOrderStatus.setText(statusVi);

            tvOrderDate.setText(item.getDate());
            tvOrderTotal.setText(item.getTotal() + " đ");
            tvOrderContact.setText(item.getContactInfo());
            tvOrderProducts.setText(item.getProductListString());
        }
    }
}
