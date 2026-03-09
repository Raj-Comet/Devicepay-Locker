# EMI Shield Locker - Android Enterprise Device Owner Application

**Version:** 1.0.0  
**Programming Language:** Kotlin  
**Minimum SDK:** API 26+  
**Target SDK:** API 34

## 🎯 Project Overview

EMI Shield Locker is a production-ready Android Enterprise Device Owner application that enforces financial device policies. It locks devices based on EMI payment status from a backend API and prevents uninstallation through Device Admin policies.

### Key Features

✅ **Device Owner Mode** - Full device control with enterprise policies  
✅ **EMI Status API Integration** - Real-time payment status checks  
✅ **Lock Screen Overlay** - Prevents device use for unpaid EMI  
✅ **Uninstall Protection** - Cannot be removed by users  
✅ **Background Service** - Continuous EMI status monitoring  
✅ **WorkManager Integration** - Periodic EMI checks (15 minutes)  
✅ **Kiosk Mode** - Single/Multi-app restrictions  
✅ **Factory Reset Protection** - Survives device resets  
✅ **MVVM Architecture** - Clean, maintainable codebase  
✅ **Retrofit API Client** - Robust HTTP communication

---

## 📁 Project Structure

```
EMIShieldLocker/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/emishield/locker/
│   │   │   │   ├── admin/
│   │   │   │   │   └── DeviceAdminReceiver.kt
│   │   │   │   ├── locker/
│   │   │   │   │   ├── LockActivity.kt
│   │   │   │   │   └── LockScreenManager.kt
│   │   │   │   ├── kiosk/
│   │   │   │   │   └── KioskManager.kt
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   └── RetrofitClient.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── EmiCheckService.kt
│   │   │   │   │   ├── EmiCheckWorker.kt
│   │   │   │   │   └── BootReceiver.kt
│   │   │   │   ├── repository/
│   │   │   │   │   └── EmiRepository.kt
│   │   │   │   ├── model/
│   │   │   │   │   └── EmiStatusResponse.kt
│   │   │   │   ├── utils/
│   │   │   │   │   └── DevicePolicyUtils.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── MainViewModel.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── activity_lock.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── styles.xml
│   │   │   │   ├── xml/
│   │   │   │   │   └── device_admin_policy.xml
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── backend/
│   ├── server.js
│   ├── routes.js
│   ├── routes/
│   │   └── emiControllers.js
│   ├── models/
│   │   └── deviceModel.js
│   └── package.json
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Electric Eel or newer)
- **JDK 17+**
- **Android SDK** (API 34)
- **Kotlin 1.9.0+**
- **Node.js 16+** (for backend server)

### Android Setup

1. **Clone/Open Project**
   ```bash
   cd EMIShieldLocker
   ```

2. **Build Project**
   ```bash
   ./gradlew clean build
   ```

3. **Generate APK**
   ```bash
   ./gradlew assembleRelease
   ```
   APK location: `app/build/outputs/apk/release/app-release.apk`

4. **Update Backend URL** (if needed)
   Edit `app/src/main/kotlin/com/emishield/locker/api/RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:3000/api/" // Change this
   ```

### Backend Setup

1. **Install Dependencies**
   ```bash
   cd backend
   npm install
   ```

2. **Start Server**
   ```bash
   npm start
   ```
   Server runs on: `http://localhost:3000`

3. **For Development (Auto-reload)**
   ```bash
   npm run dev
   ```

---

## 📱 Device Owner Setup

### Important: Factory Reset Device First

```bash
# Connect device via USB
adb devices

# Erase all data
adb shell pm disable com.google.android.gms
adb shell pm disable com.google.android.apps.wellbeing
adb shell wm overscan 0,0,0,0

# Factory reset
adb shell "am start -n android/com.android.internal.app.ResetConfirmParentActivity"
# Or simply from Settings > System > Reset options > Erase all data
```

### Method 1: ADB Device Owner Setup (Recommended)

```bash
# 1. Connect device via USB and enable USB Debugging
adb devices

# 2. Set as Device Owner before any account setup
adb shell pm grant com.emishield.locker android.permission.MANAGE_DEVICE_ADMINS
adb shell pm grant com.emishield.locker android.permission.INTERACT_ACROSS_USERS_FULL
adb shell pm grant com.emishield.locker android.permission.MANAGE_USERS

# 3. Disable Device Setup (to avoid account requirement)
adb shell settings put global device_provisioned 1
adb shell settings put secure user_setup_complete 1

# 4. Install APK
adb install app/build/outputs/apk/release/app-release.apk

# 5. Set as Device Owner
adb shell dpm set-device-owner com.emishield.locker/.admin.DeviceAdminReceiver

# 6. Verify
adb shell cmd device_policy dump | grep owner
```

### Method 2: QR Code Provisioning

1. **Generate QR Configuration**
   ```json
   {
     "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": "com.emishield.locker/.admin.DeviceAdminReceiver",
     "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": "https://example.com/app-release.apk",
     "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": "[SHA256_HASH]",
     "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE": {
       "serial": "device_serial"
     }
   }
   ```

2. **Generate QR Code** from the JSON (use any QR code generator)

3. **Scan during first boot** after factory reset

### Verify Device Owner Status

```bash
adb shell cmd device_policy dump
# Output should show:
# Device Owner: com.emishield.locker
# Active Device Admins: com.emishield.locker/.admin.DeviceAdminReceiver
```

---

## 🔌 API Integration

### Base URL
```
http://10.0.2.2:3000/api/
```
(Use `localhost:3000` for backend development)

### API Endpoints

#### 1. Register Device
```
POST /device/register
Content-Type: application/json

{
  "device_id": "ANDROID_ID",
  "model": "Samsung Galaxy S21",
  "manufacturer": "Samsung",
  "android_version": "12.0",
  "app_version": "1.0.0"
}

Response (201):
{
  "success": true,
  "device_id": "ANDROID_ID",
  "message": "Device registered successfully"
}
```

#### 2. Get EMI Status
```
GET /emi/status?device_id=ANDROID_ID

Response (200):
{
  "device_id": "ANDROID_ID",
  "status": "UNPAID",
  "amount": 15000,
  "due_days": 5,
  "next_due_date": "2026-03-15",
  "message": "EMI payment is due"
}

Status Values:
- "PAID": Device is unlocked
- "UNPAID": Device shows lock screen
```

#### 3. Update Payment
```
POST /payment/update
Content-Type: application/json

{
  "device_id": "ANDROID_ID",
  "amount": 15000,
  "transaction_id": "TXN_12345"
}

Response (200):
{
  "success": true,
  "data": "Payment updated successfully",
  "message": "Thank you for the payment!"
}
```

#### 4. Get Device Policy
```
GET /device/policy?device_id=ANDROID_ID

Response (200):
{
  "success": true,
  "data": {
    "kiosk_mode": {
      "enabled": true,
      "whitelisted_apps": ["com.emishield.locker", "com.android.dialer"]
    },
    "security_policy": {
      "disable_uninstall": true,
      "disable_factory_reset": true
    },
    "check_interval": 900000
  }
}
```

#### 5. Log Device Event
```
POST /device/log-event?device_id=ANDROID_ID&event_type=APP_INSTALLED&data=com.example.app

Response (200):
{
  "success": true,
  "data": "Event logged successfully"
}
```

---

## 🔐 Security Policies Enforced

### Device Admin Policies

| Policy | Effect |
|--------|--------|
| **Disable Uninstall** | App cannot be removed by user or system |
| **Disable Factory Reset** | User cannot factory reset device |
| **Disable Safe Boot** | Device cannot boot into safe mode |
| **Block Unknown Installs** | Only Play Store apps allowed |
| **Password Required** | Numeric PIN required (minimum 4 digits) |
| **USB Debugging Disabled** | ADB cannot connect except as Device Owner |

### Device Owner Exclusive Policies

- Full device control via `DevicePolicyManager`
- Enforces all Device Admin policies
- Kiosk mode restrictions
- Factory Reset Protection
- Lock Task restrictions

---

## 📋 Lock Flow Diagram

```
Device Boot
    ↓
BootReceiver triggers
    ↓
EmiCheckService starts (Foreground Service)
    ↓
WorkManager schedules periodic checks (every 15 min)
    ↓
Check EMI Status API
    ↓
├─→ If PAID: Unlock device, dismiss lockscreen
│
└─→ If UNPAID: Show LockActivity (fullscreen overlay)
      ↓
      User cannot exit lock screen
      ↓
      User clicks "Pay Now" → Opens payment URL
      ↓
      Payment processed → API status changes to PAID
      ↓
      Next EMI check → Device unlocks automatically
```

---

## 🧪 Testing

### Test EMI Status Manually

1. **Register Device**
   ```bash
   curl -X POST http://localhost:3000/api/device/register \
     -H "Content-Type: application/json" \
     -d '{
       "device_id": "test_device_123",
       "model": "Galaxy S21",
       "manufacturer": "Samsung"
     }'
   ```

2. **Check Status**
   ```bash
   curl http://localhost:3000/api/emi/status?device_id=test_device_123
   ```

3. **Simulate Payment**
   ```bash
   curl -X POST http://localhost:3000/api/payment/update \
     -H "Content-Type: application/json" \
     -d '{
       "device_id": "test_device_123",
       "amount": 15000,
       "transaction_id": "TXN_123"
     }'
   ```

### Test Android App

1. Install APK on test device:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. Grant permissions:
   ```bash
   adb shell pm grant com.emishield.locker android.permission.INTERACT_ACROSS_USERS_FULL
   adb shell pm grant com.emishield.locker android.permission.MANAGE_DEVICE_ADMINS
   ```

3. Open app and verify:
   - Device ID displays correctly
   - EMI status shows in UI
   - Lock screen appears for unpaid EMI

---

## 🔄 Factory Reset Protection

After factory reset, the device automatically:

1. Checks if `com.emishield.locker` is installed
2. If not, downloads and installs from configured location
3. Re-applies Device Owner policies
4. Resumes EMI monitoring

**Configuration Required:**
```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": 
  "https://your-server.com/app-release.apk"
}
```

---

## 📦 Building Release APK

### Step 1: Create Keystore

```bash
keytool -genkey -v -keystore emishield.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias emishield-key
```

### Step 2: Sign APK

```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore emishield.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  emishield-key

zipalign -v 4 app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

### Step 3: Verify Signature

```bash
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release.apk
```

---

## 🐛 Troubleshooting

### Device Lock Stuck

**Problem:** Device locked, cannot access Settings  
**Solution:**
```bash
adb shell am start -n com.emishield.locker/.locker.LockActivity
adb shell pm clear com.emishield.locker
```

### API Not Connecting

**Problem:** Getting connection refused  
**Solution:**
1. Verify backend running: `curl http://localhost:3000/health`
2. Check device URL in `RetrofitClient.kt`
3. Emulator: Use `10.0.2.2` instead of `localhost`
4. Physical device: Use actual machine IP

### Device Owner Not Set

**Problem:** Policies not enforcing  
**Solution:**
```bash
adb shell cmd device_policy dump | grep owner
# If empty, re-run device owner setup
adb shell dpm set-device-owner com.emishield.locker/.admin.DeviceAdminReceiver
```

### App Won't Install

**Problem:** Installation fails  
**Solution:**
```bash
# Clear Play Protect
adb shell pm disable-user --user 0 com.android.vending

# Try installing again
adb install -r app-release.apk
```

---

## 📝 Logging

Check app logs:
```bash
adb logcat | grep "EMI\|Admin\|Lock"
```

Backend logs:
```
Server automatically prints:
✓ Device registered
✓ EMI Status fetched
✓ Payment updated
✓ Event logged
```

---

## 🤝 Contributing

For improvements and bug fixes, please follow Android Enterprise best practices.

---

## 📄 License

MIT License - See LICENSE file

---

## 👨‍💼 Support

For issues or questions:
- Check logs: `adb logcat`
- Review API responses: Network Interceptor in Retrofit
- Verify Device Owner status: `adb shell cmd device_policy dump`
- Test backend: `curl http://localhost:3000/health`

---

## 🎓 Key Learnings

### Device Owner Mode

- Must be set before any user account setup
- Requires factory reset to change
- Full device control permissions
- Survives factory resets if configured

### Kiosk Mode

- Prevents user from accessing other apps
- Uses `DevicePolicyManager.setLockTaskPackages()`
- Required for compliant EMI lockdown

### EMI Protection Strategy

- Continuous monitoring via WorkManager
- Foreground Service for reliability
- API-driven lock state management
- User can only access payment options

### Android Enterprise Best Practices

✅ Request minimum necessary permissions  
✅ Use AppOps to monitor access  
✅ Encrypt sensitive data  
✅ Implement proper error handling  
✅ Test on actual device, not just emulator  
✅ Handle network failures gracefully  
✅ Use WorkManager for background tasks  
✅ Implement proper logging for debugging  

---

**Build Date:** March 2026  
**Last Updated:** March 10, 2026

For latest updates, visit: `https://github.com/brtprivate/interview_assignment`
