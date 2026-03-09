package com.emishield.locker.service

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emishield.locker.locker.LockScreenManager
import com.emishield.locker.repository.EmiRepository

/**
 * EMI Check Worker - Background task using WorkManager
 */
class EmiCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "EmiCheckWorker"
    private val repository = EmiRepository()

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Running EMI check worker")
            val deviceId = getDeviceId()
            
            val result = repository.getEmiStatus(deviceId)
            result.onSuccess { emiStatus ->
                Log.d(TAG, "EMI Status: ${emiStatus.status}")
                LockScreenManager.checkAndShowLock(applicationContext, emiStatus)
            }
            result.onFailure { error ->
                Log.e(TAG, "Error checking EMI status", error)
                return Result.retry()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in EMI check worker", e)
            Result.retry()
        }
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            applicationContext.contentResolver, 
            Settings.Secure.ANDROID_ID
        )
    }
}
