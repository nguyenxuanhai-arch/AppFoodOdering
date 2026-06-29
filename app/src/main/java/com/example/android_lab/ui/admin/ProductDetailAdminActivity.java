package com.example.android_lab.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.android_lab.R;
import com.example.android_lab.models.Product;

public class ProductDetailAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_admin);

        Product product = (Product) getIntent().getSerializableExtra("product");

        TextView tvName = findViewById(R.id.tvFoodNameDetail);
        TextView tvPrice = findViewById(R.id.tvFoodPriceDetail);
        TextView tvDesc = findViewById(R.id.tvFoodDescriptionDetail);
        ImageView imgFood = findViewById(R.id.imgFoodDetail);
        ImageView imgBack = findViewById(R.id.imgBackDetail);

        if (product != null) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f₫", product.getPrice()));
            tvDesc.setText(product.getDescription());

            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .into(imgFood);
        }

        imgBack.setOnClickListener(v -> finish());
    }
}
