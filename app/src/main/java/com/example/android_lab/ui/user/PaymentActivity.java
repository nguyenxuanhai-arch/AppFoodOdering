package com.example.android_lab.ui.user;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.android_lab.R;
import com.example.android_lab.models.CartItem;
import com.example.android_lab.services.ApiClient;
import com.example.android_lab.services.PaymentApiService;
import com.example.android_lab.services.PaymentRequest;
import com.example.android_lab.services.PaymentResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.app.ProgressDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private TextView tvAmount, tvNote, tvExpiredAt, btnConfirmPayment;
    private ImageView imgQr, btnBack;
    private Button btnDownloadImage;
    private String qrUrl = "";
    private long expiredAtEpoch = 0;
    private boolean successActivityOpened = false;
    private ArrayList<CartItem> cartItems;
    private Handler handler = new Handler();
    private DatabaseReference statusRef;
    private ValueEventListener statusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý thanh toán...");
        progressDialog.setCancelable(false);

        initViews();
        setupListeners();
        handleIntent();
    }

    private void initViews() {
        tvAmount = findViewById(R.id.tvAmount);
        tvNote = findViewById(R.id.tvNote);
        imgQr = findViewById(R.id.imgQr);
        btnDownloadImage = findViewById(R.id.btnDownloadImage);
        btnBack = findViewById(R.id.btnBack);
        tvExpiredAt = findViewById(R.id.tvExpiredAt);
        btnConfirmPayment = findViewById(R.id.btnProceed);
        btnDownloadImage.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnDownloadImage.setOnClickListener(v -> downloadQrImage(qrUrl));
    }

    private void handleIntent() {
        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        double amount = getIntent().getDoubleExtra("amount", 0);

        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String address = getIntent().getStringExtra("address");
        String contactInfo = String.format("Tên: %s\nSĐT: %s\nĐịa chỉ: %s", name, phone, address);

        String productListString = buildProductList();
        double total = calculateTotal();

        String userId = FirebaseAuth.getInstance().getUid();
        String orderId = "ORDER_" + System.currentTimeMillis();
        String note = "Thanh toán đơn hàng " + orderId;

        callN8nApi(userId, orderId, amount, note);

        btnConfirmPayment.setOnClickListener(v -> {
            if (successActivityOpened) return;
            btnConfirmPayment.setEnabled(false);
            progressDialog.show();
            saveOrderToFirebase(userId, orderId, amount, contactInfo, productListString, total);
            new Handler().postDelayed(this::goToSuccessAndClearCart, 2000);
        });
    }

    private String buildProductList() {
        StringBuilder builder = new StringBuilder();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                builder.append(item.getName())
                        .append(" x ").append(item.getQuantity())
                        .append(" - ")
                        .append(String.format("%,.0f₫", item.getPrice() * item.getQuantity()))
                        .append("\n");
            }
        }
        return builder.toString();
    }

    private double calculateTotal() {
        double total = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private void saveOrderToFirebase(String userId, String orderId, double amount, String contactInfo, String productList, double total) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("payments").child(userId).child(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("amount", amount);
        data.put("status", "pending");
        data.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        data.put("id", orderId);
        data.put("name", getIntent().getStringExtra("name"));
        data.put("phone", getIntent().getStringExtra("phone"));
        data.put("address", getIntent().getStringExtra("address"));
        data.put("contactInfo", contactInfo);
        data.put("productListString", productList);
        data.put("total", total);
        ref.setValue(data);
    }

    private void callN8nApi(String userId, String orderId, double amount, String note) {
        PaymentApiService api = ApiClient.getClient().create(PaymentApiService.class);
        long now = System.currentTimeMillis() / 1000;
        long expiredAt = now + 15 * 60;
        int roundedAmount = (int) Math.round(amount);

        PaymentRequest request = new PaymentRequest(userId, orderId, roundedAmount, note, expiredAt);

        api.createPayment(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse res = response.body();
                    qrUrl = res.getQrImageUrl();
                    expiredAtEpoch = res.getExpiredAt();
                    btnDownloadImage.setVisibility(View.VISIBLE);
                    tvAmount.setText(String.format("%,d₫", res.getAmount()));
                    tvNote.setText(res.getNote());
                    Glide.with(PaymentActivity.this).load(qrUrl).into(imgQr);
                } else {
                    Toast.makeText(PaymentActivity.this, "❌ Lỗi tạo mã QR", Toast.LENGTH_SHORT).show();
                    expiredAtEpoch = expiredAt;
                    tvNote.setText(note);
                }
                startCountdown();
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                expiredAtEpoch = expiredAt;
                tvNote.setText(note);
                startCountdown();
            }
        });
    }

    private void goToSuccessAndClearCart() {
        if (successActivityOpened) return;
        successActivityOpened = true;

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("cart").child(userId).removeValue();
        }

        Toast.makeText(this, "Đơn thanh toán đã được tạo. Vui lòng kiểm tra lịch sử!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void startCountdown() {
        handler.removeCallbacks(countdownRunnable);
        handler.post(countdownRunnable);
    }

    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountdown();
            handler.postDelayed(this, 1000);
        }
    };

    private void updateCountdown() {
        long now = System.currentTimeMillis() / 1000;
        long secondsLeft = expiredAtEpoch - now;

        if (secondsLeft <= 0) {
            tvExpiredAt.setText("⛔ QR đã hết hạn");
            btnDownloadImage.setEnabled(false);
            imgQr.setAlpha(0.5f);
            btnConfirmPayment.setEnabled(false);
            handler.removeCallbacks(countdownRunnable);
            return;
        }

        long minutes = secondsLeft / 60;
        long seconds = secondsLeft % 60;
        tvExpiredAt.setText(String.format(Locale.getDefault(), "⏳ Còn lại: %02d:%02d", minutes, seconds));
    }

    private void downloadQrImage(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ảnh QR để tải", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "QR_" + System.currentTimeMillis() + ".png";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Tải ảnh QR");
        request.setDescription("Đang tải mã QR về máy...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        manager.enqueue(request);

        Toast.makeText(this, "Đang tải ảnh QR về máy...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(countdownRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (expiredAtEpoch > System.currentTimeMillis() / 1000) {
            handler.post(countdownRunnable);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (statusRef != null && statusListener != null) {
            statusRef.removeEventListener(statusListener);
        }
    }
}
