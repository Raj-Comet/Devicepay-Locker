# Android Enterprise QR Provisioning Configuration

## 📱 QR Code for Device Owner Setup

This document explains how to generate and use QR codes for seamless Android Enterprise provisioning.

---

## 🔧 Configuration JSON

Save this as `provisioning-config.json`:

```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
    "com.emishield.locker/.admin.DeviceAdminReceiver",
  
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME": 
    "com.emishield.locker",
  
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": 
    "https://your-server.com/app-release.apk",
  
  "android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE": {
    "serial": "optional_device_serial"
  },
  
  "android.app.extra.PROVISIONING_MODE": 
    "PROVISIONING_MODE_FULLY_MANAGED_DEVICE",
  
  "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": false,
  
  "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": false
}
```

---

## 🔑 Generate QR Code

### Method 1: Online Tool (Recommended for Testing)

1. Go to: https://www.the-qrcode-generator.com/
2. Select "URL" or "Custom"
3. Copy full JSON string or base64 encoded JSON
4. Generate QR code
5. Test by scanning on factory-reset device

### Method 2: Python Script

```python
import json
import qrcode
import base64

config = {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
        "com.emishield.locker/.admin.DeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": 
        "https://your-server.com/app-release.apk"
}

# Convert to string
config_str = "ANDROID_MANAGEMENT:" + base64.b64encode(
    json.dumps(config).encode()
).decode()

# Generate QR
qr = qrcode.QRCode(version=1, error_correction=qrcode.constants.ERROR_CORRECT_L)
qr.add_data(config_str)
qr.make(fit=True)

img = qr.make_image()
img.save("provisioning_qr.png")
print("QR code saved as provisioning_qr.png")
```

Run:
```bash
pip install qrcode[pil] pillow
python generate_qr.py
```

### Method 3: Node.js Script

```javascript
const qr = require('qrcode');
const fs = require('fs');

const config = {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
        "com.emishield.locker/.admin.DeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": 
        "https://your-server.com/app-release.apk"
};

const data = "ANDROID_MANAGEMENT:" + Buffer.from(
    JSON.stringify(config)
).toString('base64');

qr.toFile('provisioning_qr.png', data, (err) => {
    if (err) console.error(err);
    console.log('QR code generated!');
});
```

Install and run:
```bash
npm install qrcode
node generate_qr.js
```

---

## 📲 Using QR Code for Provisioning

### Step 1: Factory Reset Device

1. Power off device
2. Hold: Power + Volume Down
3. Select "Wipe data/factory reset"
4. Reboot device

### Step 2: Welcome Screen Setup

1. Device shows "Welcome to Android"
2. **DO NOT** skip through setup
3. At "Work Profile" or Enterprise provisioning screen:

### Step 3: Scan QR Code

1. Tap provisioning button (varies by device)
2. Camera opens automatically
3. Scan QR code from screen/printout
4. Wait for provisioning to start

### Step 4: Automatic Installation

Device will:
1. Download APK from configured URL
2. Install `com.emishield.locker`
3. Make app Device Owner
4. Apply security policies
5. Lock device
6. Start EMI monitoring

---

## 📝 Manual Provisioning (Without QR)

If QR scanning doesn't work:

```bash
# Connect device via USB during setup
adb shell am start -n com.android.managedprovisioning/.PreProvisioningActivity

# Or via ADB force
adb shell dpm set-device-owner com.emishield.locker/.admin.DeviceAdminReceiver
```

---

## 🎯 QR Code Best Practices

### 1. Encoding
- Use base64 encoding for special characters
- Keep JSON compact (remove extra spaces)
- Test with multiple devices

### 2. Security
```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM":
    "SHA256_HASH_OF_APP_CERTIFICATE"
}
```

Generate checksum:
```bash
# Get APK certificate
keytool -printcert -jarfile app-release.apk

# Or compute SHA256
sha256sum app-release.apk
```

### 3. Testing QR

Before deployment:
- Test on Pixel device (best support)
- Test on Samsung device
- Test on Chinese device (Xiaomi/Redmi)
- Document any issues

---

## 🌐 Sample QR for Testing

### Quick Test QR (Minimal Config)

```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
    "com.emishield.locker/.admin.DeviceAdminReceiver"
}
```

Generate and test separately first.

---

## 📋 Troubleshooting

### QR Not Scanning

1. **Ensure proper encoding:**
   ```bash
   echo -n 'YOUR_JSON' | base64
   ```

2. **Use proper prefix:**
   ```
   ANDROID_MANAGEMENT:BASE64_ENCODED_JSON
   ```

3. **Test with ZXing decoder:**
   https://zxing.org/w/decode.jspx

### Provisioning Fails

- Ensure device is factory-reset
- APK download URL is accessible
- App package name matches configuration
- Device admin component path is correct

### Device Owner Not Set

```bash
# Verify
adb shell cmd device_policy dump | grep owner

# Force set (if needed)
adb shell dpm set-device-owner com.emishield.locker/.admin.DeviceAdminReceiver
```

---

## 🔗 Links & Resources

- **Android Enterprise Documentation:**  
  https://developer.android.com/work/enterprise

- **Device Provisioning:**  
  https://developer.android.com/work/managed-provisioning

- **QR Code Generators:**  
  https://www.the-qrcode-generator.com/

- **ZXing QR Decoder:**  
  https://zxing.org/w/decode.jspx

---

## 📊 Configuration Examples

### Minimal (QR Only)
```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
    "com.emishield.locker/.admin.DeviceAdminReceiver"
}
```

### Full Featured
```json
{
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME": 
    "com.emishield.locker/.admin.DeviceAdminReceiver",
  
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION": 
    "https://your-server.com/app-release.apk",
  
  "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM": 
    "sha256_hash_here",
  
  "android.app.extra.PROVISIONING_SKIP_ENCRYPTION": false,
  
  "android.app.extra.PROVISIONING_MODE": 
    "PROVISIONING_MODE_FULLY_MANAGED_DEVICE"
}
```

---

**Version:** 1.0  
**Last Updated:** March 2026

For support: https://github.com/brtprivate/interview_assignment
