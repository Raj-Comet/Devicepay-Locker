package com.emishield.locker.locker

import android.content.Context
import android.content.Intent
import android.util.Log
import com.emishield.locker.model.EmiStatusResponse

/**
 * Lock Screen Manager - Manages lock screen display based on EMI status
 */
object LockScreenManager {

    private const val TAG = "LockScreenManager"

    /**
     * Show lock screen if EMI payment is pending
     */
    fun checkAndShowLock(context: Context, emiStatus: EmiStatusResponse) {
        if (!emiStatus.isPaid()) {
            showLockScreen(context, emiStatus)
        } else {
            dismissLockScreen(context)
        }
    }

    /**
     * Show lock screen activity
     */
    fun showLockScreen(context: Context, emiStatus: EmiStatusResponse) {
        Log.d(TAG, "Showing lock screen for unpaid EMI")
        val intent = Intent(context, LockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            putExtra("due_days", emiStatus.dueDays)
            putExtra("amount", emiStatus.amount)
            putExtra("message", emiStatus.message.ifEmpty { "Your device is locked due to pending EMI payment" })
        }
        context.startActivity(intent)
    }

    /**
     * Dismiss lock screen
     */
    fun dismissLockScreen(context: Context) {
        Log.d(TAG, "Dismissing lock screen")
        // The lock activity will finish when payment is confirmed
    }

    /**
     * Log lock event to repository
     */
    fun logLockEvent(context: Context, dueDays: Int) {
        Log.d(TAG, "Lock screen displayed. Days due: $dueDays")
    }

    /**
     * Check if lock screen is currently showing
     */
    fun isLockScreenActive(): Boolean {
        // This can be checked using activity manager or shared preferences
        return true // Placeholder
    }
}
