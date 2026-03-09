const express = require('express');
const router = express.Router();
const { Device, EmiRecord } = require('../models/deviceModel');

// POST /device/register - Register a new device
const registerDevice = (req, res) => {
  try {
    const { device_id, model, manufacturer, android_version, app_version } = req.body;
    
    if (!device_id || !model) {
      return res.status(400).json({
        success: false,
        message: 'device_id and model are required'
      });
    }
    
    const device = new Device(device_id, model, manufacturer || 'Unknown');
    Device.devices.set(device_id, device);
    
    // Initialize EMI record
    const emiRecord = {
      deviceId: device_id,
      status: 'PAID',
      amount: 0,
      dueDays: 0,
      nextDueDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      message: 'Device registered successfully. EMI protection activated.'
    };
    EmiRecord.emiRecords.set(device_id, emiRecord);
    
    console.log(`✓ Device registered: ${device_id} (${model})`);
    
    res.status(201).json({
      success: true,
      device_id: device_id,
      message: 'Device registered successfully'
    });
  } catch (error) {
    console.error('Error registering device:', error);
    res.status(500).json({
      success: false,
      message: 'Error registering device',
      error: error.message
    });
  }
};

// GET /emi/status - Get EMI status for a device
const getEmiStatus = (req, res) => {
  try {
    const { device_id } = req.query;
    
    if (!device_id) {
      return res.status(400).json({
        success: false,
        message: 'device_id query parameter is required'
      });
    }
    
    const emiRecord = EmiRecord.emiRecords.get(device_id);
    
    if (!emiRecord) {
      // Return default paid status for unregistered devices
      return res.status(200).json({
        device_id: device_id,
        status: 'PAID',
        due_days: 0,
        amount: 0,
        next_due_date: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        message: 'Device not registered yet. Please register first.'
      });
    }
    
    console.log(`✓ EMI Status fetched for: ${device_id} - Status: ${emiRecord.status}`);
    
    res.status(200).json(emiRecord);
  } catch (error) {
    console.error('Error fetching EMI status:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching EMI status',
      error: error.message
    });
  }
};

// POST /payment/update - Update payment status
const updatePayment = (req, res) => {
  try {
    const { device_id, amount, transaction_id } = req.body;
    
    if (!device_id || !amount || !transaction_id) {
      return res.status(400).json({
        success: false,
        message: 'device_id, amount, and transaction_id are required'
      });
    }
    
    let emiRecord = EmiRecord.emiRecords.get(device_id) || {
      deviceId: device_id,
      status: 'PAID',
      amount: 0,
      dueDays: 0,
      nextDueDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      message: ''
    };
    
    // Update EMI record
    emiRecord.status = 'PAID';
    emiRecord.amount = 0;
    emiRecord.dueDays = 0;
    emiRecord.message = `Payment of ₹${amount} received successfully. Transaction ID: ${transaction_id}`;
    
    EmiRecord.emiRecords.set(device_id, emiRecord);
    
    console.log(`✓ Payment updated: ${device_id} - Amount: ₹${amount}, Transaction: ${transaction_id}`);
    
    res.status(200).json({
      success: true,
      data: 'Payment updated successfully',
      message: 'Thank you for the payment!'
    });
  } catch (error) {
    console.error('Error updating payment:', error);
    res.status(500).json({
      success: false,
      message: 'Error updating payment',
      error: error.message
    });
  }
};

// GET /device/policy - Get device policy
const getDevicePolicy = (req, res) => {
  try {
    const { device_id } = req.query;
    
    const policy = {
      success: true,
      data: {
        kiosk_mode: {
          enabled: false,
          whitelisted_apps: [
            'com.emishield.locker',
            'com.android.dialer',
            'com.android.settings'
          ]
        },
        security_policy: {
          disable_uninstall: true,
          disable_factory_reset: true,
          disable_safe_boot: true,
          require_password: true
        },
        check_interval: 900000 // 15 minutes in ms
      }
    };
    
    res.status(200).json(policy);
  } catch (error) {
    console.error('Error fetching device policy:', error);
    res.status(500).json({
      success: false,
      message: 'Error fetching device policy',
      error: error.message
    });
  }
};

// POST /device/log-event - Log device event
const logDeviceEvent = (req, res) => {
  try {
    const { device_id, event_type, data } = req.query;
    
    const eventLog = {
      device_id: device_id,
      event_type: event_type,
      data: data,
      timestamp: new Date().toISOString()
    };
    
    console.log(`✓ Event logged: [${event_type}] ${device_id} - ${data}`);
    
    res.status(200).json({
      success: true,
      data: 'Event logged successfully'
    });
  } catch (error) {
    console.error('Error logging event:', error);
    res.status(500).json({
      success: false,
      message: 'Error logging event',
      error: error.message
    });
  }
};

// Setup Routes
router.post('/device/register', registerDevice);
router.get('/emi/status', getEmiStatus);
router.post('/payment/update', updatePayment);
router.get('/device/policy', getDevicePolicy);
router.post('/device/log-event', logDeviceEvent);

module.exports = router;
