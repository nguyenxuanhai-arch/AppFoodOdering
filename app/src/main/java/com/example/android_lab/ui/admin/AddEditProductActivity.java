package com.example.android_lab.ui.admin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.android_lab.R;
import com.example.android_lab.models.Product;
import com.example.android_lab.utils.ImageHelper;
import com.example.android_lab.utils.ImageUploader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddEditProductActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCT = "product";

    private EditText etName, etPrice, etDescription, etQuantity;
    private RadioGroup rgType;
    private RadioButton rbFood, rbDrink;
    private Switch btnSwitch;
    private Button btnAdd, btnPickImage;
    private ImageView imgPreview, imgBack;
    private Uri imageUri;
    private boolean isUploading = false;
    private Product editingProduct = null;
    private DatabaseReference productRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private ProgressDialog progressDialog; // ✅ Loading Dialog

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        initViews();

        progressDialog = new ProgressDialog(this); // ✅ Init
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        productRef = FirebaseDatabase.getInstance().getReference("products");
        initImagePicker();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PRODUCT)) {
            editingProduct = (Product) intent.getSerializableExtra(EXTRA_PRODUCT);
            fillFormForEdit(editingProduct);
            btnAdd.setText("Cập nhật sản phẩm");
            btnAdd.setOnClickListener(v -> updateProduct());
        } else {
            btnAdd.setText("Thêm sản phẩm");
            btnAdd.setOnClickListener(v -> addProduct());
        }

        btnPickImage.setOnClickListener(v -> ImageHelper.openGallery(imagePickerLauncher));
        imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBackAdd);
        etName = findViewById(R.id.etProductName);
        etPrice = findViewById(R.id.etProductPrice);
        etDescription = findViewById(R.id.etProductDescription);
        etQuantity = findViewById(R.id.etProductQuantity);
        btnAdd = findViewById(R.id.btnAddProduct);
        btnSwitch = findViewById(R.id.btnSwitch);
        btnPickImage = findViewById(R.id.btnPickImage);
        imgPreview = findViewById(R.id.imgPreview);
        rgType = findViewById(R.id.rgType);
        rbFood = findViewById(R.id.rbFood);
        rbDrink = findViewById(R.id.rbDrink);
    }

    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgPreview.setVisibility(ImageView.VISIBLE);
                        imgPreview.setImageURI(imageUri);
                    }
                }
        );
    }

    private void fillFormForEdit(Product product) {
        etName.setText(product.getName());
        etPrice.setText(String.valueOf(product.getPrice()));
        etDescription.setText(product.getDescription());
        etQuantity.setText(String.valueOf(product.getQuantity()));
        if ("food".equals(product.getType())) rbFood.setChecked(true);
        else rbDrink.setChecked(true);
        btnSwitch.setChecked(product.isPopular());

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imgPreview.setVisibility(ImageView.VISIBLE);
            Glide.with(this).load(product.getImageUrl()).into(imgPreview);
        }
    }

    private void addProduct() {
        if (isUploading) return;

        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = rbFood.isChecked() ? "food" : "drink";
        boolean isPopular = btnSwitch.isChecked();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());

        if (!validateInput(name, priceStr, imageUri)) return;

        isUploading = true;
        btnAdd.setEnabled(false);
        progressDialog.show(); // ✅ Show loading

        double price = Double.parseDouble(priceStr);
        String productId = productRef.push().getKey();

        if (productId == null) {
            showToast("Không tạo được ID món");
            isUploading = false;
            btnAdd.setEnabled(true);
            progressDialog.dismiss(); // ✅ Dismiss
            return;
        }

        Product product = new Product(productId, name, price, "", isPopular, description, quantity, type);

        ImageUploader.uploadImageProduct(imageUri, productId, new ImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                product.setImageUrl(downloadUrl);
                saveProductData(product);
            }

            @Override
            public void onFailure(Exception e) {
                isUploading = false;
                btnAdd.setEnabled(true);
                progressDialog.dismiss(); // ✅ Dismiss
                showToast("Lỗi upload ảnh: " + e.getMessage());
            }
        });
    }

    private void updateProduct() {
        if (editingProduct == null) return;

        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String type = rbFood.isChecked() ? "food" : "drink";
        boolean isPopular = btnSwitch.isChecked();
        int quantity = Integer.parseInt(etQuantity.getText().toString().trim());

        if (imageUri != null) {
            isUploading = true;
            btnAdd.setEnabled(false);
            progressDialog.show(); // ✅ Show loading

            ImageUploader.uploadImageProduct(imageUri, editingProduct.getId(), new ImageUploader.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Product updated = new Product(editingProduct.getId(), name, Double.parseDouble(priceStr), downloadUrl, isPopular, description, quantity, type);
                    saveUpdatedProduct(updated);
                }

                @Override
                public void onFailure(Exception e) {
                    isUploading = false;
                    btnAdd.setEnabled(true);
                    progressDialog.dismiss(); // ✅ Dismiss
                    showToast("Lỗi upload ảnh: " + e.getMessage());
                }
            });
        } else {
            Product updated = new Product(editingProduct.getId(), name, Double.parseDouble(priceStr), editingProduct.getImageUrl(), isPopular, description, quantity, type);
            saveUpdatedProduct(updated);
        }
    }

    private void saveUpdatedProduct(Product updated) {
        productRef.child(updated.getId()).setValue(updated)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss(); // ✅ Dismiss
                    showToast("Cập nhật thành công");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // ✅ Dismiss
                    showToast("Lỗi cập nhật: " + e.getMessage());
                    isUploading = false;
                    btnAdd.setEnabled(true);
                });
    }

    private boolean validateInput(String name, String priceStr, Uri imageUri) {
        if (name.isEmpty() || priceStr.isEmpty() || imageUri == null) {
            showToast("Vui lòng nhập đầy đủ và chọn ảnh");
            return false;
        }
        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showToast("Giá không hợp lệ");
            return false;
        }
        return true;
    }

    private void saveProductData(Product product) {
        productRef.child(product.getId()).setValue(product)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss(); // ✅ Dismiss
                    showToast("Thêm sản phẩm thành công");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // ✅ Dismiss
                    showToast("Lỗi thêm sản phẩm: " + e.getMessage());
                    isUploading = false;
                    btnAdd.setEnabled(true);
                });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
