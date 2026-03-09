const express = require('express');
const bodyParser = require('body-parser');
const emiRoutes = require('./routes/emiRoutes');
const { Device, EmiRecord } = require('./models/deviceModel');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// CORS middleware
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});

// Request logging
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/api', emiRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'OK',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    message: 'EMI Locker Backend Server is running'
  });
});

// Root endpoint
app.get('/', (req, res) => {
  res.status(200).json({
    message: 'EMI Shield Locker Backend API',
    version: '1.0.0',
    documentation: 'Please refer to README.md for API documentation'
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: 'Endpoint not found'
  });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    success: false,
    message: 'Internal server error',
    error: process.env.NODE_ENV === 'development' ? err.message : 'An error occurred'
  });
});

// Initialize in-memory data
const initializeData = () => {
  // Sample devices
  Device.devices.set('device_123', new Device('device_123', 'Samsung Galaxy S21', 'Samsung'));
  Device.devices.set('device_456', new Device('device_456', 'Redmi Note 10', 'Xiaomi'));
  
  // Sample EMI records
  EmiRecord.emiRecords.set('device_123', {
    deviceId: 'device_123',
    status: 'UNPAID',
    amount: 15000,
    dueDays: 5,
    nextDueDate: '2026-03-15',
    message: 'EMI payment is due'
  });
  
  EmiRecord.emiRecords.set('device_456', {
    deviceId: 'device_456',
    status: 'PAID',
    amount: 0,
    dueDays: 0,
    nextDueDate: '2026-04-10',
    message: 'Payment received. Thank you!'
  });
};

// Start server
initializeData();
app.listen(PORT, () => {
  console.log(`\n╔═════════════════════════════════════════╗`);
  console.log(`║  EMI Shield Locker Backend Server       ║`);
  console.log(`║  Version: 1.0.0                         ║`);
  console.log(`║  Server running on http://localhost:${PORT}   ║`);
  console.log(`╚═════════════════════════════════════════╝\n`);
  console.log('Available endpoints:');
  console.log('  GET  /health - Health check');
  console.log('  GET  /api/emi/status?device_id=xxx - Get EMI status');
  console.log('  POST /api/device/register - Register device');
  console.log('  POST /api/payment/update - Update payment status');
  console.log('  GET  /api/device/policy?device_id=xxx - Get device policy');
  console.log('  POST /api/device/log-event - Log device event\n');
});

module.exports = app;
