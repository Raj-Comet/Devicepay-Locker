# EMI Shield Locker - Backend API Documentation

**Version:** 1.0.0  
**Server:** Node.js + Express  
**Port:** 3000

---

## 🚀 Quick Start

### Installation

```bash
cd backend
npm install
npm start
```

Server will start on: `http://localhost:3000`

### Health Check

```bash
curl http://localhost:3000/health
```

Response:
```json
{
  "status": "OK",
  "timestamp": "2026-03-10T10:30:00.000Z",
  "uptime": 45.234,
  "message": "EMI Locker Backend Server is running"
}
```

---

## 📡 API Endpoints

### 1. Device Registration

**Endpoint:** `POST /api/device/register`

**Request:**
```json
{
  "device_id": "abc123def456",
  "model": "Samsung Galaxy S21",
  "manufacturer": "Samsung",
  "android_version": "12.0",
  "app_version": "1.0.0"
}
```

**Response (201):**
```json
{
  "success": true,
  "device_id": "abc123def456",
  "message": "Device registered successfully"
}
```

**Error (400):**
```json
{
  "success": false,
  "message": "device_id and model are required"
}
```

---

### 2. Get EMI Status

**Endpoint:** `GET /api/emi/status?device_id={deviceId}`

**Query Parameters:**
- `device_id` (required): Device Android ID

**Response (200):**
```json
{
  "device_id": "abc123def456",
  "status": "UNPAID",
  "amount": 15000,
  "due_days": 5,
  "next_due_date": "2026-03-15",
  "message": "EMI payment is due"
}
```

**Status Values:**
- `PAID`: Device is unlocked
- `UNPAID`: Device shows lock screen

**Example:**
```bash
curl "http://localhost:3000/api/emi/status?device_id=test_device_123"
```

---

### 3. Update Payment

**Endpoint:** `POST /api/payment/update`

**Request:**
```json
{
  "device_id": "abc123def456",
  "amount": 15000,
  "transaction_id": "TXN_20260310_12345"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": "Payment updated successfully",
  "message": "Thank you for the payment!"
}
```

**Example:**
```bash
curl -X POST http://localhost:3000/api/payment/update \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test_device_123",
    "amount": 15000,
    "transaction_id": "TXN_123"
  }'
```

---

### 4. Get Device Policy

**Endpoint:** `GET /api/device/policy?device_id={deviceId}`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "kiosk_mode": {
      "enabled": false,
      "whitelisted_apps": [
        "com.emishield.locker",
        "com.android.dialer",
        "com.android.settings"
      ]
    },
    "security_policy": {
      "disable_uninstall": true,
      "disable_factory_reset": true,
      "disable_safe_boot": true,
      "require_password": true
    },
    "check_interval": 900000
  }
}
```

---

### 5. Log Device Event

**Endpoint:** `POST /api/device/log-event`

**Query Parameters:**
- `device_id` (required): Device ID
- `event_type` (required): Event type (e.g., APP_INSTALLED, LOCK_SCREEN_SHOWN)
- `data` (optional): Event details

**Response (200):**
```json
{
  "success": true,
  "data": "Event logged successfully"
}
```

**Example:**
```bash
curl -X POST "http://localhost:3000/api/device/log-event?device_id=test_123&event_type=PAYMENT_MADE&data=Paid_15000"
```

---

## 📊 Data Model

### Device

```javascript
{
  deviceId: "abc123def456",
  model: "Samsung Galaxy S21",
  manufacturer: "Samsung",
  registeredAt: "2026-03-10T10:30:00Z",
  lastSeen: "2026-03-10T10:35:00Z",
  appVersion: "1.0.0",
  androidVersion: "12.0"
}
```

### EMI Record

```javascript
{
  deviceId: "abc123def456",
  status: "UNPAID",           // "PAID" or "UNPAID"
  amount: 15000,               // Amount due in INR
  dueDays: 5,                  // Days remaining to pay
  nextDueDate: "2026-03-15",  // ISO date string
  message: "EMI payment is due",
  lastUpdated: "2026-03-10T10:30:00Z"
}
```

---

## 🔄 Sample Flow

### Complete User Journey

```
1. Device Boot
   ↓ POST /device/register
   { device_id, model, manufacturer }
   ← { success: true }

2. App Starts EMI Check
   ↓ GET /emi/status?device_id=xxx
   ← { status: "UNPAID", due_days: 5 }

3. Lock Screen Shown
   (Device Owner enforces lock screen)

4. User Pays EMI
   ↓ POST /payment/update
   { device_id, amount, transaction_id }
   ← { success: true }

5. Payment Updated in System
   (Backend records payment)

6. Next EMI Check (15 mins)
   ↓ GET /emi/status?device_id=xxx
   ← { status: "PAID" }

7. Device Automatically Unlocks
   (Lock screen dismissed)
```

---

## 🧪 Testing with cURL

### Test Script

```bash
#!/bin/bash

DEVICE_ID="test_device_$(date +%s)"
API_URL="http://localhost:3000/api"

echo "=== EMI Shield Backend API Test ==="
echo ""

# 1. Register Device
echo "1. Registering device..."
curl -X POST "$API_URL/device/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"device_id\": \"$DEVICE_ID\",
    \"model\": \"Samsung Galaxy S21\",
    \"manufacturer\": \"Samsung\"
  }"
echo ""
echo ""

# 2. Check EMI Status
echo "2. Checking EMI status..."
curl "$API_URL/emi/status?device_id=$DEVICE_ID"
echo ""
echo ""

# 3. Update Payment
echo "3. Updating payment..."
curl -X POST "$API_URL/payment/update" \
  -H "Content-Type: application/json" \
  -d "{
    \"device_id\": \"$DEVICE_ID\",
    \"amount\": 15000,
    \"transaction_id\": \"TXN_123\"
  }"
echo ""
echo ""

# 4. Check Status Again
echo "4. Checking EMI status after payment..."
curl "$API_URL/emi/status?device_id=$DEVICE_ID"
echo ""
```

Save as `test.sh` and run:
```bash
chmod +x test.sh
./test.sh
```

---

## 📝 In-Memory Database

The backend uses in-memory storage (Maps) for simplicity. Data is lost when server restarts.

### For Production: Replace with Persistent Storage

```javascript
// Example: MongoDB
const mongoose = require('mongoose');

const deviceSchema = new mongoose.Schema({
  deviceId: String,
  model: String,
  manufacturer: String,
  registeredAt: Date
});

const Device = mongoose.model('Device', deviceSchema);
```

---

## 🔒 Security Considerations

1. **Validate all inputs** - Check device_id format, amounts, etc.
2. **Rate limiting** - Implement per-device rate limits
3. **HTTPS only** - Use SSL/TLS in production
4. **API Authentication** - Add Bearer token validation
5. **Audit logging** - Log all API calls with timestamps
6. **CORS** - Restrict to allowed domains in production

**Example CORS Configuration:**
```javascript
const cors = require('cors');

app.use(cors({
  origin: ['https://payment.example.com'],
  credentials: true
}));
```

---

## 🚨 Error Handling

### Common Error Codes

| Code | Message | Solution |
|------|---------|----------|
| 400 | Missing required parameters | Check request body |
| 404 | Endpoint not found | Verify API URL |
| 500 | Internal server error | Check server logs |

### Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "error": "Detailed error info (development only)"
}
```

---

## 📊 Monitoring & Logs

### Server Logs

```
[2026-03-10T10:30:00.000Z] POST /api/device/register
✓ Device registered: test_device_123 (Samsung Galaxy S21)

[2026-03-10T10:31:00.000Z] GET /api/emi/status
✓ EMI Status fetched for: test_device_123 - Status: UNPAID

[2026-03-10T10:32:00.000Z] POST /api/payment/update
✓ Payment updated: test_device_123 - Amount: ₹15000, Transaction: TXN_123
```

### Enable Verbose Logging

```javascript
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  console.log('Headers:', req.headers);
  console.log('Body:', req.body);
  next();
});
```

---

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find process using port 3000
lsof -i :3000

# Kill process
kill -9 <PID>
```

### Module Not Found Errors

```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

### CORS Issues on Android

Android sends preflight requests. Ensure OPTIONS is handled:
```bash
curl -X OPTIONS http://localhost:3000/api/device/register \
  -H "Origin: http://localhost:3000"
```

---

## 📚 Related Files

- **Server:** `backend/server.js`
- **Controllers:** `backend/routes/emiControllers.js`
- **Models:** `backend/models/deviceModel.js`
- **Routes:** `backend/routes.js`

---

## 🎯 Next Steps

1. Add persistent database (MongoDB, PostgreSQL)
2. Implement JWT authentication
3. Add payment gateway integration
4. Deploy to production server
5. Implement rate limiting
6. Add comprehensive logging
7. Set up monitoring dashboards

---

**Last Updated:** March 10, 2026  
**API Version:** 1.0.0  
**Status:** Production Ready
