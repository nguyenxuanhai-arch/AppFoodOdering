# 🍽️ App Food Ordering

Ứng dụng đặt món ăn trực tuyến dành cho Android, cho phép người dùng duyệt menu, đặt hàng và thanh toán qua QR code tích hợp công nghệ n8n Webhook.

## 📱 Tổng Quan Ứng Dụng

**App Food Ordering** là một ứng dụng Android hiện đại được xây dựng với **Java** và tích hợp:

- 📋 **Xem Menu & Danh Mục**: Duyệt danh sách các món ăn được phân loại rõ ràng
- 🛒 **Giỏ Hàng**: Quản lý đơn hàng trước khi thanh toán
- 💳 **Thanh Toán QR**: Quét QR code để xử lý thanh toán an toàn qua n8n Webhook
- 🔄 **Xử Lý Webhook n8n**: Tích hợp Retrofit để gọi API n8n, xử lý logic thanh toán và kiểm tra trạng thái đơn hàng
- 🔐 **Quản Trị**: Bảng điều khiển quản lý đơn hàng (admin panel)
- 🔔 **Firebase**: Hỗ trợ thông báo và dữ liệu cloud

---

## 🎯 Luồng Thanh Toán QR

```
1. Quét QR Code (Ứng dụng)
        ↓
2. Gửi payload qua Retrofit → n8n Webhook
        ↓
3. n8n Workflow xử lý (kiểm tra, ghi log, gọi gateway)
        ↓
4. Trả về kết quả thanh toán
        ↓
5. Cập nhật trạng thái đơn hàng trên App
```

---

## 🚀 Hướng Dẫn Cài Đặt & Setup

### Yêu Cầu Hệ Thống

- **Android Studio**: Phiên bản Dolphin trở lên
- **Java**: JDK 11 hoặc cao hơn
- **Android SDK**: API level 24+ (Android 7.0 trở lên)
- **n8n Account**: Tài khoản n8n Cloud hoặc self-hosted n8n instance
- **Firebase Project**: (Tùy chọn) Cấu hình Firebase nếu muốn dùng thông báo

---

### ⚙️ Bước 1: Clone Repository

```bash
git clone https://github.com/nguyenxuanhai-arch/AppFoodOdering.git
cd AppFoodOdering
```

---

### ⚙️ Bước 2: Mở Dự Án trong Android Studio

1. Mở **Android Studio**
2. Chọn **File → Open** và chỉ đến thư mục `AppFoodOdering`
3. Android Studio sẽ tự động load project và sync Gradle
4. Chờ quá trình **Gradle Build** hoàn tất (có thể mất vài phút)

---

### ⚙️ Bước 3: Cấu Hình n8n Webhook

#### **Lựa Chọn A: Sử Dụng n8n Cloud (Khuyến Nghị - Không Cần Cài Đặt)**

**Ưu điểm**: Nhanh, không cần cài đặt local, có HTTPS sẵn, hoàn toàn miễn phí

1. Truy cập [n8n.io](https://n8n.io) và đăng ký/đăng nhập
2. Tạo **Workspace** mới hoặc dùng workspace hiện tại
3. Chọn **Workflows** → **Import from File**
4. Import file `Myworkflow` từ repo:
   - Tìm thư mục gốc repo, file tên `Myworkflow.json` (hoặc tương tự)
5. Mở workflow vừa import, kiểm tra **Webhook Node**:
   - **Method**: POST
   - **Path**: `payment/qr`
   - **Mode**: Chọn **Production** (hoặc **Test** để thử)
6. Nhấn **Save** → **Activate Workflow**
7. **Sao chép Webhook URL** từ Webhook Node:
   - **Test URL**: Dùng cho môi trường thử nghiệm (chứa `/webhook-test/...`)
   - **Production URL**: Dùng cho môi trường chính thức (chứa `/webhook/...`)

#### **Lựa Chọn B: Self-Hosted n8n (Local Development)**

Nếu muốn chạy n8n trên máy tính local:

**Yêu cầu**: Docker đã cài đặt

```bash
# Chạy n8n qua Docker
docker run -it --rm \
  -p 5678:5678 \
  -v ~/.n8n:/home/node/.n8n \
  --name n8n \
  n8nio/n8n
```

- Mở http://localhost:5678 trên trình duyệt
- Đăng ký tài khoản ban đầu
- Import file `Myworkflow` → Save → Activate
- **Webhook URL** (Test): `http://localhost:5678/webhook-test/payment/qr`
- **Webhook URL** (Prod): `http://localhost:5678/webhook/payment/qr`

---

### ⚙️ Bước 4: Cấu Hình App

#### **Bước 4.1: Cấu Hình Webhook URL trong Build Config**

Mở file `app/build.gradle` (Module: app):

```groovy
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.example.appfoodordering"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
        
        // ⭐ Thêm Webhook URL (chọn một trong hai)
        // Nếu dùng n8n Cloud:
        buildConfigField "String", "N8N_WEBHOOK_URL", "\"https://your-n8n-cloud-webhook-url\""
        
        // Nếu dùng n8n Local (emulator):
        // buildConfigField "String", "N8N_WEBHOOK_URL", "\"http://10.0.2.2:5678/webhook-test/payment/qr\""
    }
    
    buildTypes {
        debug {
            // Dev: n8n Cloud Test URL hoặc Local
            buildConfigField "String", "N8N_WEBHOOK_URL", "\"https://your-n8n-cloud-webhook-url-test\""
        }
        release {
            // Sản xuất: n8n Cloud Production URL
            buildConfigField "String", "N8N_WEBHOOK_URL", "\"https://your-n8n-cloud-webhook-url-prod\""
        }
    }
}
```

**Lưu ý**: 
- Thay `https://your-n8n-cloud-webhook-url` bằng URL thực tế từ n8n Cloud
- Nếu dùng emulator Android kết nối n8n local, dùng `10.0.2.2` thay vì `localhost`

#### **Bước 4.2: Cấu Hình Firebase (Tùy Chọn)**

Nếu muốn dùng Firebase:

1. Truy cập [Firebase Console](https://console.firebase.google.com)
2. Tạo project mới hoặc dùng project hiện tại
3. Thêm ứng dụng Android:
   - **Package Name**: `com.example.appfoodordering`
   - Download file `google-services.json`
4. Đặt `google-services.json` vào thư mục `app/`
5. Đã thêm Firebase dependency trong `build.gradle`

---

### ⚙️ Bước 5: Dependencies & Build

File `app/build.gradle` đã có sẵn các dependencies:

```groovy
dependencies {
    // Retrofit & Networking
    implementation 'com.squareup.retrofit2:retrofit:2.11.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
    
    // Firebase (nếu dùng)
    implementation platform('com.google.firebase:firebase-bom:33.1.2')
    implementation 'com.google.firebase:firebase-analytics'
    
    // Android UI & Utilities
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.google.android.material:material:1.9.0'
}
```

**Chạy Gradle Sync**:
- Android Studio sẽ tự sync khi mở project
- Hoặc: **File** → **Sync Now**

---

### ⚙️ Bước 6: Cấu Hình Emulator / Thiết Bị

#### **Sử Dụng Emulator Android**

1. Mở **Device Manager** trong Android Studio
2. Tạo emulator mới hoặc chọn emulator hiện tại
3. **Lưu ý kết nối localhost**:
   - Emulator truy cập `localhost` của máy tính qua IP: `10.0.2.2`
   - Nếu n8n local chạy tại `http://localhost:5678`, trong emulator dùng `http://10.0.2.2:5678`

#### **Sử Dụng Thiết Bị Thật**

1. Kết nối thiết bị Android qua USB
2. Bật **USB Debugging** trên thiết bị (Settings → Developer Options → USB Debugging)
3. Nếu muốn kết nối n8n local từ thiết bị:
   - Tìm IP LAN của máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
   - Dùng IP đó thay vì `localhost` (vd: `http://192.168.1.10:5678`)

---

### ⚙️ Bước 7: Build & Chạy App

#### **Cách 1: Dùng Android Studio**

1. Chọn emulator/thiết bị từ dropdown
2. Nhấn **Run** (▶️) hoặc **Shift+F10**
3. Chờ app build & cài đặt

#### **Cách 2: Dùng Command Line**

```bash
# Debug build
./gradlew installDebug

# Release build
./gradlew installRelease
```

---

## 🧪 Kiểm Thử Webhook

### Kiểm Thử Nhanh với curl

Trước khi chạy app, hãy test Webhook n8n:

```bash
# Nếu dùng n8n Cloud Test URL
curl -X POST "https://your-n8n-cloud-webhook-test-url" \
  -H "Content-Type: application/json" \
  -d '{
    "qrData": "orderId=12345&amount=150000",
    "amount": 150000,
    "currency": "VND",
    "orderId": "12345",
    "meta": {
      "source": "android-debug"
    }
  }'

# Nếu dùng n8n Local
curl -X POST "http://localhost:5678/webhook-test/payment/qr" \
  -H "Content-Type: application/json" \
  -d '{
    "qrData": "orderId=12345&amount=150000",
    "amount": 150000,
    "currency": "VND",
    "orderId": "12345",
    "meta": {
      "source": "android-debug"
    }
  }'
```

**Phản hồi mong đợi**:
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

### Kiểm Thử với Postman

1. Mở **Postman**
2. Tạo request **POST** mới
3. Dán Webhook URL vào URL bar
4. Chọn tab **Body** → **raw** → **JSON**
5. Dán payload trên
6. Nhấn **Send**

---

## 🏗️ Cấu Trúc Dự Án

```
AppFoodOdering/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/appfoodordering/
│   │   │   │       ├── api/              # Retrofit API interfaces
│   │   │   │       ├── model/            # Data models (Request/Response)
│   │   │   │       ├── activity/         # UI Activities
│   │   │   │       ├── fragment/         # UI Fragments
│   │   │   │       └── util/             # Utilities
│   │   │   ├── res/                      # Resources (layouts, strings, etc)
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle
├── Myworkflow                            # n8n Workflow (import to n8n)
├── README.md                             # This file
└── .gitignore
```

---

## 📄 API Contract (Hợp Đồng)

### Request

```json
{
  "qrData": "string (QR code data)",
  "amount": "long (Số tiền, đơn vị: VND)",
  "currency": "string (Đơn vị tiền: VND, USD, ...)",
  "orderId": "string (Mã đơn hàng)",
  "meta": {
    "source": "string (Nguồn: android-debug, android-release, ...)",
    "extra": "object (Thông tin bổ sung tuỳ chọn)"
  }
}
```

### Response

```json
{
  "status": "string (ok, error, pending, ...)",
  "message": "string (Chi tiết kết quả)",
  "orderId": "string (Mã đơn hàng)",
  "amount": "long (Số tiền xử lý)",
  "currency": "string (Đơn vị tiền)",
  "processedAt": "string (ISO 8601 timestamp)"
}
```

---

## 🔐 Thông Tin Đăng Nhập Quản Trị

> ⚠️ **Chỉ dùng cho môi trường Dev/Test. KHÔNG công khai trên sản xuất.**

- **Email**: name@gmail.com
- **Password**: 123456

---

## 🛡️ Ghi Chú Bảo Mật

1. **HTTPS Bắt Buộc**: 
   - Dev: có thể dùng HTTP (với networkSecurityConfig)
   - Prod: Bắt buộc HTTPS (n8n Cloud đã hỗ trợ)

2. **Bảo Vệ Webhook**:
   - Thêm API Key / Basic Auth / JWT token
   - Hạn chế IP hoặc Reverse Proxy với rate limit
   - Xác thực origin request

3. **Không Log Dữ Liệu Nhạy Cảm**:
   - Tránh log: số thẻ, mã bí mật, thông tin cá nhân
   - Sử dụng masked data trong logs

4. **Error Handling**:
   - Không expose internal error messages cho client
   - Log error đầy đủ phía server (n8n/backend)

---

## 🤝 Hỗ Trợ & Troubleshooting

### Emulator không kết nối được n8n local
- ✅ Kiểm tra n8n đang chạy: `http://localhost:5678`
- ✅ Dùng `10.0.2.2` thay vì `localhost` trong emulator
- ✅ Firewall: mở port 5678

### App crash khi gọi Webhook
- ✅ Kiểm tra **Webhook URL** trong `BuildConfig`
- ✅ Xem **Logcat**: Search cho `retrofit` hoặc `IOException`
- ✅ Test curl trước: đảm bảo n8n endpoint hoạt động

### Firebase không hoạt động
- ✅ Đặt `google-services.json` vào `app/` folder
- ✅ Build → Clean Project → Rebuild

### Gradle sync fail
- ✅ **File** → **Invalidate Caches** → Restart
- ✅ Xóa `.gradle` folder, sync lại

---

## 📚 Tài Liệu Tham Khảo

- [n8n Documentation](https://docs.n8n.io)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Android Developer Guide](https://developer.android.com)
- [Firebase Documentation](https://firebase.google.com/docs)

---

## 📝 License

Dự án này được sử dụng cho mục đích học tập và phát triển. Vui lòng liên hệ tác giả để biết thêm chi tiết.

---

**Cập nhật lần cuối**: 2025
**Tác giả**: nguyenxuanhai-arch
