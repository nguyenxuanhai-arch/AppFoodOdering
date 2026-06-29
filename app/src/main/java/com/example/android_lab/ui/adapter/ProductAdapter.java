package com.example.android_lab.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.example.android_lab.ui.admin.ProductDetailAdminActivity;
import com.example.android_lab.ui.user.ProductDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final DatabaseReference cartBaseRef;
    private OnProductActionListener actionListener;
    private final boolean isAdmin;


    public ProductAdapter(Context context, List<Product> productList, boolean isAdmin) {
        this.context = context;
        this.productList = productList;
        this.isAdmin = isAdmin;
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        this.cartBaseRef = FirebaseDatabase.getInstance().getReference("cart").child(uid);
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = isAdmin ? R.layout.item_product_admin : R.layout.item_product;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ProductViewHolder(view, actionListener, isAdmin);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        holder.bind(context, productList.get(position), cartBaseRef);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice;
        Button btnAddToCart;
        ImageView btnMenu;
        private final boolean isAdmin;
        private final OnProductActionListener actionListener;

        public ProductViewHolder(View itemView, OnProductActionListener actionListener, boolean isAdmin) {
            super(itemView);
            this.actionListener = actionListener;
            this.isAdmin = isAdmin;

            imgProduct = itemView.findViewById(R.id.imgProduct) != null ? itemView.findViewById(R.id.imgProduct) : itemView.findViewById(R.id.imgAdminProduct);
            tvName = itemView.findViewById(R.id.tvProductName) != null ? itemView.findViewById(R.id.tvProductName) : itemView.findViewById(R.id.tvAdminProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice) != null ? itemView.findViewById(R.id.tvProductPrice) : itemView.findViewById(R.id.tvAdminProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart); // chỉ có ở layout user
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(Context context, Product product, DatabaseReference cartBaseRef) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f₫", product.getPrice()));
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(imgProduct);

            // Người dùng: xử lý thêm giỏ
            if (!isAdmin && btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> addToCart(context, cartBaseRef, product));
            }

            itemView.setOnClickListener(v -> {
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(context, ProductDetailAdminActivity.class);
                } else {
                    intent = new Intent(context, ProductDetailActivity.class);
                }
                intent.putExtra("product", product);
                context.startActivity(intent);
            });

            // Admin: hiển thị popup menu
            if (isAdmin && btnMenu != null) {
                btnMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, btnMenu);
                    popup.getMenuInflater().inflate(R.menu.menu_product_actions, popup.getMenu());
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_edit) {
                            if (actionListener != null) {
                                actionListener.onEditProduct(product);
                            }
                            return true;
                        } else if (item.getItemId() == R.id.action_delete) {
                            if (actionListener != null) {
                                actionListener.onDeleteProduct(product);
                            }
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }

        private void addToCart(Context context, DatabaseReference cartBaseRef, Product product) {
            if (product.getId() == null) {
                Toast.makeText(context, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference cartRef = cartBaseRef.child(product.getId());

            cartRef.get().addOnSuccessListener(snapshot -> {
                int currentQty = 0;
                if (snapshot.exists()) {
                    Product existing = snapshot.getValue(Product.class);
                    currentQty = (existing != null) ? existing.getQuantity() : 0;
                }

                product.setQuantity(currentQty + 1);
                if (product.getDescription() == null) product.setDescription("Không có mô tả");
                product.setType("product");

                cartRef.setValue(product)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context, "Đã thêm vào giỏ", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Lỗi thêm giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );

            }).addOnFailureListener(e ->
                    Toast.makeText(context, "Lỗi đọc giỏ: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    public interface OnProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }
}
