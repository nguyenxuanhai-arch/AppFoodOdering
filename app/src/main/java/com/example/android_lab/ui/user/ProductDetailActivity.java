package com.example.android_lab.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProduct, imgBack;
    private TextView tvName, tvPrice, tvDescription ,btnAddToCart, btnBuyNow;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail); // dùng lại layout hiện tại

        imgProduct = findViewById(R.id.imgFoodDetail);
        tvName = findViewById(R.id.tvFoodNameDetail);
        tvPrice = findViewById(R.id.tvFoodPriceDetail);
        tvDescription = findViewById(R.id.tvFoodDescriptionDetail);
        btnAddToCart = findViewById(R.id.btnAddToCartDetail);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        imgBack = findViewById(R.id.imgBackDetail);

        product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            showProductDetails(product);
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());
        imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void showProductDetails(Product product) {
        tvName.setText(product.getName());
        tvPrice.setText(String.format("%,.0f₫", product.getPrice()));
        tvDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .into(imgProduct);
    }

    private void addToCart() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("cart").child(uid).child(product.getId());

        cartRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Product existingProduct = snapshot.getValue(Product.class);
                int currentQuantity = (existingProduct != null) ? existingProduct.getQuantity() : 0;
                product.setQuantity(currentQuantity + 1);
            } else {
                product.setQuantity(1);
            }

            product.setType("product"); // ✅ Bắt buộc phải set

            cartRef.setValue(product)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi đọc giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void buyNow() {
        if (product == null) return;

        if (product.getQuantity() <= 0) {
            product.setQuantity(1);
        }

        product.setType("product");

        double total = product.getPrice() * product.getQuantity();

        Intent intent = new Intent(this, ConfirmOrderActivity.class);
        intent.putExtra("amount", total);

        ArrayList<Product> singleItem = new ArrayList<>();
        singleItem.add(product);
        intent.putExtra("cartItems", singleItem);

        startActivity(intent);
    }

}
