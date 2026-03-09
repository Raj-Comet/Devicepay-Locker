package com.emishield.locker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.emishield.locker.R
import com.emishield.locker.locker.LockScreenManager
import com.emishield.locker.repository.EmiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * EMI Check Service - Runs in foreground and checks EMI status periodically
 */
class EmiCheckService : Service() {

    private val TAG = "EmiCheckService"
    private val NOTIFICATION_ID = 1
    private val NOTIFICATION_CHANNEL_ID = "emi_check_channel"
    private val CHECK_INTERVAL = 15 * 60 * 1000L // 15 minutes
    
    private lateinit var handler: Handler
    private lateinit var repository: EmiRepository
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "EMI Check Service Created")
        handler = Handler(Looper.getMainLooper())
        repository = EmiRepository()
        createNotificationChannel()
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "EMI Check Service Started")
        startEmiChecking()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "EMI Check Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Monitors EMI payment status"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startEmiChecking() {
        handler.postDelayed(::checkEmiStatus, CHECK_INTERVAL)
    }

    private fun checkEmiStatus() {
        Log.d(TAG, "Checking EMI status...")
        val deviceId = getDeviceId()
        
        scope.launch {
            val result = repository.getEmiStatus(deviceId)
            result.onSuccess { emiStatus ->
                Log.d(TAG, "EMI Status: ${emiStatus.status}")
                LockScreenManager.checkAndShowLock(this@EmiCheckService, emiStatus)
            }
            result.onFailure { error ->
                Log.e(TAG, "Error checking EMI status", error)
            }
        }

        // Schedule next check
        handler.postDelayed(::checkEmiStatus, CHECK_INTERVAL)
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "EMI Check Service Destroyed")
        handler.removeCallbacksAndMessages(null)
    }
}
