package com.emishield.locker.kiosk

import android.app.ActivityManager
import android.content.Context
import android.util.Log

/**
 * Kiosk Mode Manager
 * Enables single app or multi-app kiosk mode using LockTask
 */
class KioskManager(private val context: Context) {

    private val TAG = "KioskManager"

    /**
     * Enable kiosk mode for a single app
     */
    fun enableSingleAppKiosk(packageName: String) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.startLockTask()
            Log.d(TAG, "Single app kiosk enabled for: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling single app kiosk", e)
        }
    }

    /**
     * Enable multi-app kiosk mode
     */
    fun enableMultiAppKiosk(vararg packageNames: String) {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.startLockTask()
            Log.d(TAG, "Multi-app kiosk enabled for: ${packageNames.joinToString(", ")}")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling multi-app kiosk", e)
        }
    }

    /**
     * Disable kiosk mode
     */
    fun disableKiosk() {
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.stopLockTask()
            Log.d(TAG, "Kiosk mode disabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling kiosk mode", e)
        }
    }

    /**
     * Check if kiosk mode is active
     */
    fun isKioskModeActive(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } catch (e: Exception) {
            Log.e(TAG, "Error checking kiosk mode status", e)
            false
        }
    }

    /**
     * Get locked task mode state
     */
    fun getLockedTaskModeState(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.lockTaskModeState
        } catch (e: Exception) {
            Log.e(TAG, "Error getting locked task mode state", e)
            ActivityManager.LOCK_TASK_MODE_NONE
        }
    }
}
