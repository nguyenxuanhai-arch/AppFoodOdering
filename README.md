# Tích hợp n8n Webhook + Retrofit để xử lý QR code thanh toán (Android Java)

Tài liệu này cập nhật mục tiêu: Ứng dụng Android (Java) sẽ sử dụng Retrofit để gọi API Webhook do n8n cung cấp nhằm xử lý QR code thanh toán. Firebase vẫn dùng […]

Điểm cập nhật quan trọng:
- Không cần tự tạo/cấu hình workflow thủ công: repo đã có sẵn file “Myworkflow” để import.
- Không bắt buộc cài n8n bằng Docker. Bạn có thể dùng n8n Cloud trên website n8n.io (không cần cài đặt máy chủ), rất phù hợp vì bài toán chỉ xử lý QR đơn giản.

Mục lục
- Tổng quan luồng xử lý
- Dùng workflow n8n có sẵn (Myworkflow) — n8n Cloud hoặc self-hosted
- Hợp đồng API (request/response)
- Tích hợp Retrofit trong Android app (Java)
- Cấu hình base URL và HTTP (HTTP/HTTPS, emulator)
- Build & chạy nhanh
- Thông tin đăng nhập quản trị
- Ghi chú bảo mật và kiểm thử

---

## 1) Tổng quan luồng xử lý

1. Ứng dụng quét QR -> nhận được chuỗi QR (qrData) + thông tin đơn hàng (orderId, amount, currency).
2. App gọi API Webhook n8n qua Retrofit: POST tới URL Webhook (prod hoặc test).
3. n8n:
   - Nhận payload từ Webhook.
   - Kiểm tra/chuẩn hóa dữ liệu (Function node).
   - (Tuỳ chọn) Gọi gateway thanh toán/ghi log/ghi CSDL qua các node n8n khác.
   - Trả về JSON kết quả qua node Respond to Webhook.
4. App nhận response và cập nhật UI/trạng thái thanh toán.

---

## 2) Dùng workflow n8n có sẵn (Myworkflow) — n8n Cloud hoặc self-hosted

Bạn có 2 lựa chọn, trong đó n8n Cloud (n8n.io) là cách đơn giản nhất, không cần cài đặt:

### 2.1) Khuyến nghị: n8n Cloud (không cần cài đặt/Docker)
- Truy cập n8n.io, đăng ký/đăng nhập và vào không gian làm việc của bạn.
- Chọn “Workflows” -> “Import from File”, rồi chọn file “Myworkflow” trong repo này để import.
- Mở workflow vừa import, kiểm tra Webhook node:
  - Method: POST
  - Path: payment/qr
  - Mode: Production (endpoint prod) hoặc Test (endpoint test)
- Lưu (Save) và Activate workflow.
- Sao chép Webhook URL hiển thị trong Webhook node:
  - URL Test: dùng cho thử nghiệm (thường có “/webhook-test/…”)
  - URL Production: dùng cho môi trường chạy thật (thường có “/webhook/…”)
- Dùng chính URL này trong ứng dụng Android hoặc công cụ kiểm thử (curl/Postman).

Kiểm thử nhanh (Cloud):
```bash
# Thay <CLOUD_WEBHOOK_TEST_URL> bằng URL Test copy từ node Webhook
curl -X POST "<CLOUD_WEBHOOK_TEST_URL>" \
  -H "Content-Type: application/json" \
  -d '{
    "qrData": "orderId=12345&amount=150000&note=demo",
    "amount": 150000,
    "currency": "VND",
    "meta": {"source":"android-debug"}
  }'
```

### 2.2) Tùy chọn: Self-hosted (local/dev)
Bạn vẫn có thể chạy n8n local nếu muốn:
```bash
docker run -it --rm \
  -p 5678:5678 \
  -v ~/.n8n:/home/node/.n8n \
  --name n8n \
  n8nio/n8n
```
- Mở giao diện n8n local: http://localhost:5678
- Import file “Myworkflow” -> Save -> Activate.
- Test nhanh (test mode):
```bash
curl -X POST http://localhost:5678/webhook-test/payment/qr \
  -H "Content-Type: application/json" \
  -d '{
    "qrData": "orderId=12345&amount=150000&note=demo",
    "amount": 150000,
    "currency": "VND",
    "meta": {"source":"android-debug"}
  }'
```

Ghi chú:
- Với n8n Cloud, luôn dùng nguyên Webhook URL được hiển thị (copy từ Webhook node).
- Với self-hosted, endpoint mặc định là:
  - Test: http://<host>:5678/webhook-test/payment/qr
  - Prod: http://<host>:5678/webhook/payment/qr

---

## 3) Hợp đồng API (contract)

- Headers: Content-Type: application/json
- Request (ví dụ):
```json
{
  "qrData": "orderId=12345&amount=150000",
  "amount": 150000,
  "currency": "VND",
  "orderId": "12345",
  "meta": {
    "source": "android",
    "extra": "optional"
  }
}
```
- Response (ví dụ):
```json
{
  "status": "ok",
  "message": "QR processed",
  "orderId": "12345",
  "amount": 150000,
  "currency": "VND",
  "processedAt": "2025-08-10T10:02:35.000Z"
}
```

Lưu ý: Tùy cách bạn cấu hình node “Respond to Webhook”, response có thể là object normalize tùy chỉnh như trên.

---

## 4) Tích hợp Retrofit trong Android app (Java)

Thêm dependency (app/build.gradle):
```groovy
dependencies {
    implementation platform('com.google.firebase:firebase-bom:33.1.2')
    implementation 'com.google.firebase:firebase-analytics'

    implementation 'com.squareup.retrofit2:retrofit:2.11.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
}
```

Có 2 cách sử dụng với Retrofit:

- Cách A (self-hosted/local): Dùng BASE_URL + path cố định
```groovy
android {
    defaultConfig {
        // Emulator Android trỏ localhost máy tính qua 10.0.2.2
        buildConfigField "String", "N8N_BASE_URL", "\"http://10.0.2.2:5678/\""
    }
    buildTypes {
        debug {
            buildConfigField "String", "N8N_BASE_URL", "\"http://10.0.2.2:5678/\""
        }
        release {
            // Sản xuất nên dùng HTTPS nếu self-hosted (đặt domain có TLS)
            buildConfigField "String", "N8N_BASE_URL", "\"https://your-n8n-domain/\""
        }
    }
}
```
Interface:
```java
public interface N8nApi {
    @POST("webhook/payment/qr")
    Call<PaymentQrResponse> sendPaymentQr(@Body PaymentQrRequest body);
}
```

- Cách B (khuyến nghị cho n8n Cloud): Truyền full Webhook URL bằng @Url
Ưu điểm: bạn copy nguyên URL từ Webhook node (Test/Prod) mà không cần cố định BASE_URL/path.
```java
public interface N8nApiDynamic {
    @POST
    Call<PaymentQrResponse> sendPaymentQr(@Url String webhookUrl, @Body PaymentQrRequest body);
}
```
Sử dụng:
```java
// webhookUrl lấy từ Webhook node (Test hoặc Prod) trên n8n Cloud
String webhookUrl = BuildConfig.N8N_WEBHOOK_URL; // hoặc Remote Config/Encrypted storage
api.sendPaymentQr(webhookUrl, requestBody).enqueue(...);
```
Gợi ý buildConfig cho Cloud:
```groovy
android {
    defaultConfig {
        // Lưu ý: đây là FULL URL (https://.../webhook-test/payment/qr hoặc /webhook/...)
        buildConfigField "String", "N8N_WEBHOOK_URL", "\"https://<your-cloud-webhook-url>\""
    }
}
```

Models (ví dụ rút gọn):
```java
public class PaymentQrRequest {
    public String qrData;
    public Long amount;
    public String currency;
    public String orderId;
    public Map<String, Object> meta;
}

public class PaymentQrResponse {
    public String status;
    public String message;
    public String orderId;
    public Long amount;
    public String currency;
    public String processedAt;
}
```

---

## 5) Cấu hình HTTP/HTTPS và emulator

- Emulator Android truy cập “localhost” của máy tính qua IP đặc biệt: 10.0.2.2
- Thiết bị thật: dùng IP LAN của máy tính (vd: http://192.168.1.10:5678)
- Android 9+ chặn cleartext HTTP theo mặc định:
  - Dev: có thể bật cleartext cho host nội bộ bằng networkSecurityConfig.
  - Prod: Bắt buộc dùng HTTPS (n8n Cloud đã có sẵn HTTPS).

---

## 6) Build & chạy nhanh

- Mở dự án với Android Studio, sync Gradle.
- Chọn 1 trong 2:
  - n8n Cloud: Import “Myworkflow”, Activate, copy Webhook URL và dùng trực tiếp trong app/curl.
  - Self-hosted: Chạy n8n local (tùy chọn), import “Myworkflow”, dùng endpoint local.
- Run app -> quét QR -> app gọi Webhook n8n qua Retrofit.

---

## 7) Thông tin đăng nhập quản trị

- Email: name@gmail.com
- Mật khẩu: 123456

Chỉ sử dụng cho môi trường dev/test. Không công khai trên sản xuất.

---

## 8) Ghi chú bảo mật và kiểm thử

- Với môi trường public, dùng HTTPS và bổ sung cơ chế bảo vệ Webhook (secret key/Basic Auth/JWT).
- Hạn chế IP hoặc đặt Reverse Proxy với rate limit (self-hosted).
- Không log thông tin nhạy cảm (thẻ, mã bí mật) trong n8n hoặc app.
- Kiểm thử bằng curl/Postman trước khi tích hợp app để xác thực hợp đồng API.