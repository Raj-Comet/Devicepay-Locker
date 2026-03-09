package com.emishield.locker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Boot Receiver - Restarts EMI check service on device boot
 */
class BootReceiver : android.content.BroadcastReceiver() {

    override fun onReceive(context: android.content.Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, starting EMI service")
            context?.let {
                val serviceIntent = Intent(it, EmiCheckService::class.java)
                it.startService(serviceIntent)
                
                // Also schedule EMI check worker
                scheduleEmiCheckWorker(it)
            }
        }
    }

    private fun scheduleEmiCheckWorker(context: android.content.Context) {
        val emiCheckRequest = PeriodicWorkRequestBuilder<EmiCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "emi_check_work",
            ExistingPeriodicWorkPolicy.KEEP,
            emiCheckRequest
        )
    }
}
