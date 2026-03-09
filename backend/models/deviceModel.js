/**
 * Device Model - In-memory storage for devices and EMI records
 */

class Device {
  constructor(deviceId, model, manufacturer = 'Unknown') {
    this.deviceId = deviceId;
    this.model = model;
    this.manufacturer = manufacturer;
    this.registeredAt = new Date();
    this.lastSeen = new Date();
    this.appVersion = '1.0.0';
    this.androidVersion = 'Unknown';
  }
}

class EmiRecord {
  constructor(deviceId) {
    this.deviceId = deviceId;
    this.status = 'PAID'; // PAID or UNPAID
    this.amount = 0;
    this.dueDays = 0;
    this.nextDueDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    this.message = 'Device is in good standing';
    this.lastUpdated = new Date();
  }
}

// In-memory storage
Device.devices = new Map();
EmiRecord.emiRecords = new Map();

module.exports = {
  Device,
  EmiRecord
};
