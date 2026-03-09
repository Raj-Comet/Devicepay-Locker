# EMI Shield Locker - Deployment Guide

**Version:** 1.0.0  
**Target Environment:** Production Android Devices

---

## 🚀 Pre-Deployment Checklist

- [ ] APK signed with release keystore
- [ ] Backend API deployed and tested
- [ ] Device Owner setup script prepared
- [ ] QR provisioning configured
- [ ] Documentation reviewed
- [ ] Security policies verified
- [ ] API endpoints verified
- [ ] Error handling tested

---

## 📦 Build & Sign APK

### 1. Generate Keystore

```bash
keytool -genkey -v -keystore emishield-release.keystore \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias emishield-release

# Follow prompts:
# Keystore password: ****
# Key password: ****
# Common Name: Your Name
# Organization: Your Company
```

### 2. Configure Gradle Signing

Create `app/signing.properties`:
```properties
KEYSTORE_FILE=../emishield-release.keystore
KEYSTORE_PASSWORD=your_password
KEY_ALIAS=emishield-release
KEY_PASSWORD=your_key_password
```

Add to `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        keyAlias = System.getenv("KEY_ALIAS") ?: "emishield-release"
        keyPassword = System.getenv("KEY_PASSWORD")?.toCharArray()
        storeFile = file("../emishield-release.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        minifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
}
```

### 3. Build Release APK

```bash
cd EMIShieldLocker
./gradlew clean assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### 4. Verify Signature

```bash
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release.apk
```

---

## 🌐 Backend Deployment

### Option 1: Deploy to Heroku

```bash
# Install Heroku CLI
# https://devcenter.heroku.com/articles/heroku-cli

heroku login
heroku create emishield-locker-api
cd backend
git push heroku main

# Verify
heroku open
```

### Option 2: Deploy to AWS EC2

```bash
# SSH to instance
ssh -i key.pem ec2-user@your-instance-ip

# Install Node.js
curl -sL https://rpm.nodesource.com/setup_16.x | bash -
sudo yum install nodejs

# Clone and setup
git clone https://github.com/yourusername/emishield-backend.git
cd emishield-backend
npm install
npm start

# Use PM2 for auto-restart
npm install -g pm2
pm2 start server.js --name "emishield-api"
pm2 startup
pm2 save
```

### Option 3: Docker Container

Create `Dockerfile`:
```dockerfile
FROM node:16-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
EXPOSE 3000
CMD ["node", "server.js"]
```

Build and run:
```bash
docker build -t emishield-backend .
docker run -d -p 3000:3000 \
  -e PORT=3000 \
  --name emishield-api \
  emishield-backend
```

---

## 📱 Device Owner Setup at Scale

### For Single Device

```bash
# Factory reset device first
adb shell "am start -n content://com.android.internal.app.ResetConfirmParentActivity"
# Or Settings > System > Advanced > Reset options > Erase all data

# Set as Device Owner
adb shell dpm set-device-owner \
  com.emishield.locker/.admin.DeviceAdminReceiver

# Push APK (ensure it's done BEFORE account setup)
adb install -r app-release.apk
```

### For Multiple Devices (Bulk Setup)

Create `setup_devices.sh`:
```bash
#!/bin/bash

# Device serial numbers
DEVICES=("ABC123" "DEF456" "GHI789")

for DEVICE in "${DEVICES[@]}"
do
    echo "Setting up device: $DEVICE"
    
    # Connect
    adb -s $DEVICE wait-for-device
    
    # Factory reset
    adb -s $DEVICE shell "am start -n android/com.android.internal.app.ResetConfirmParentActivity"
    
    sleep 30  # Wait for reset
    
    # Set Device Owner
    adb -s $DEVICE shell dpm set-device-owner \
        com.emishield.locker/.admin.DeviceAdminReceiver
    
    # Install APK
    adb -s $DEVICE install -r app-release.apk
    
    echo "✓ Device $DEVICE setup complete"
    echo ""
done
```

Run:
```bash
chmod +x setup_devices.sh
./setup_devices.sh
```

---

## 🔄 API Configuration

Update backend URL in app before building:

Edit `app/src/main/kotlin/com/emishield/locker/api/RetrofitClient.kt`:

```kotlin
// Production
private const val BASE_URL = "https://emishield-api.herokuapp.com/api/"

// Or your AWS/custom server
private const val BASE_URL = "https://api.emishield.com/api/"
```

Rebuild APK after change:
```bash
./gradlew clean assembleRelease
```

---

## 🔐 Security Hardening

### 1. API Security

```kotlin
// Add SSL pinning to RetrofitClient
val certificatePinner = CertificatePinner.Builder()
    .add("emishield-api.herokuapp.com", "sha256/.....")
    .add("api.emishield.com", "sha256/.....")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .build()
```

Get certificate pin:
```bash
openssl s_client -connect emishield-api.herokuapp.com:443 -showcerts
```

### 2. Data Encryption

```kotlin
// Encrypt sensitive data
val encryptedDeviceId = EncryptedSharedPreferences.create(
    context,
    "emishield_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 3. Manifest Hardening

```xml
<!-- In AndroidManifest.xml -->
<application
    android:allowBackup="false"
    android:debuggable="false"
    android:icon="@mipmap/ic_launcher"
    android:usesCleartextTraffic="false">
</application>
```

### 4. ProGuard Configuration

Already configured in `app/proguard-rules.pro`. Verify:
```bash
./gradlew assembleRelease --stacktrace
```

---

## 📊 Monitoring & Logging

### Enable Remote Logging

```kotlin
class RemoteLogger {
    companion object {
        suspend fun logEvent(
            deviceId: String,
            eventType: String,
            message: String
        ) {
            repository.logEvent(deviceId, eventType, message)
        }
    }
}

// In MainActivity.kt
RemoteLogger.logEvent(deviceId, "APP_STARTED", "Application started")
```

### Backend Logging

Check logs:
```bash
# Heroku
heroku logs -t

# Docker
docker logs emishield-api

# AWS EC2
tail -f server.log
```

---

## 🧪 Production Testing Checklist

- [ ] EMI status API responds correctly
- [ ] Lock screen appears for unpaid status
- [ ] Device unlocks after payment simulation
- [ ] Background service continues after reboot
- [ ] WorkManager reschedules after device restart
- [ ] App survives factory reset
- [ ] Device Owner permissions preserved
- [ ] No crashes in logs
- [ ] API errors handled gracefully
- [ ] Battery usage acceptable

---

## 🐛 Rollback Procedure

If issues occur in production:

### Quick Rollback

```bash
# Disable app on devices
adb shell pm disable-user com.emishield.locker

# Unset Device Owner (requires USB)
adb shell dpm clear-device-owner-app
```

### Deploy Previous Version

```bash
git checkout previous-tag
./gradlew assembleRelease
# Re-deploy APK and APK
```

---

## 📈 Update & Maintenance

### Push Updates

```bash
# Increment version in build.gradle.kts
versionCode = 2
versionName = "1.0.1"

# Build
./gradlew clean assembleRelease

# Upload to server
adb install -r app-release.apk
```

### Background Updates (Over-the-Air)

Implement check at app startup:
```kotlin
// In MainActivity
ViewModel.checkForUpdates()

// ViewModel
fun checkForUpdates() {
    scope.launch {
        val latestVersion = repository.getLatestVersion()
        if (latestVersion > currentVersion) {
            showUpdateDialog()
        }
    }
}
```

---

## 📞 Support & Maintenance

### Daily Checklist

- [ ] Backend server health: `curl /health`
- [ ] Check error logs: `heroku logs --tail`
- [ ] Verify API endpoints responding
- [ ] Monitor device connections

### Monthly Checklist

- [ ] Review security logs
- [ ] Update dependencies
- [ ] Backup database
- [ ] Performance review
- [ ] User feedback analysis

---

## 🎓 Deployment Best Practices

✅ **Test thoroughly** before production  
✅ **Keep backups** of all releases  
✅ **Document all changes** in changelog  
✅ **Monitor logs** continuously  
✅ **Have rollback plan** ready  
✅ **Update dependencies** regularly  
✅ **Secure all credentials** (use env vars)  
✅ **Test with real devices** not just emulators  

---

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Code reviewed
- [ ] All tests passing
- [ ] APK size acceptable (~8-12 MB)
- [ ] No ProGuard warnings
- [ ] API tested manually
- [ ] Backend tested with load
- [ ] Documentation updated
- [ ] Team briefed

### Deployment Day
- [ ] Monitor server metrics
- [ ] Check app telemetry
- [ ] Review crash logs
- [ ] Test on sample device
- [ ] Verify lock functionality
- [ ] Check API response times

### Post-Deployment
- [ ] Monitor error rates
- [ ] Track user feedback
- [ ] Review performance metrics
- [ ] Plan for next iteration
- [ ] Document lessons learned

---

**Last Updated:** March 2026  
**Version:** 1.0.0  
**Status:** Ready for Production Deployment
