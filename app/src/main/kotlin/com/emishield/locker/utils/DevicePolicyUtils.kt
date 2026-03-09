package com.emishield.locker.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.util.Log
import com.emishield.locker.admin.DeviceAdminReceiver

/**
 * Device Policy Utilities for enforcing security policies
 */
object DevicePolicyUtils {

    private const val TAG = "DevicePolicyUtils"

    fun applyDevicePolicies(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = DeviceAdminReceiver.getComponentName(context)

        try {
            // Check if this app is the device owner
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                Log.d(TAG, "Application is Device Owner")
                applyDeviceOwnerPolicies(context, dpm, componentName)
            } else if (dpm.isAdminActive(componentName)) {
                Log.d(TAG, "Application is Device Admin (not Device Owner)")
                applyDeviceAdminPolicies(context, dpm, componentName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying device policies", e)
        }
    }

    private fun applyDeviceAdminPolicies(
        context: Context,
        dpm: DevicePolicyManager,
        componentName: ComponentName
    ) {
        try {
            // Disable uninstall
            dpm.setUninstallBlocked(componentName, context.packageName, true)
            Log.d(TAG, "Uninstall blocked")

            // Set password requirements
            dpm.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC)
            dpm.setPasswordMinimumLength(componentName, 4)
            Log.d(TAG, "Password policy set")

        } catch (e: Exception) {
            Log.e(TAG, "Error in device admin policies", e)
        }
    }

    private fun applyDeviceOwnerPolicies(
        context: Context,
        dpm: DevicePolicyManager,
        componentName: ComponentName
    ) {
        try {
            // Disable uninstall
            dpm.setUninstallBlocked(componentName, context.packageName, true)
            Log.d(TAG, "Uninstall blocked")

            // Disable factory reset
            dpm.setFactoryResetProtectionPolicy(
                componentName,
                DevicePolicyManager.FACTORY_RESET_PROTECTION_ENABLED
            )
            Log.d(TAG, "Factory reset disabled")

            // Disable safe boot
            try {
                dpm.setKeyguardDisabledFeatures(
                    componentName,
                    DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
                )
                Log.d(TAG, "Keyguard features disabled")
            } catch (e: Exception) {
                Log.w(TAG, "Could not disable keyguard features", e)
            }

            // Block unknown source installs
            dpm.setSecureSettingEnabled(componentName, "install_non_market_apps", false)
            Log.d(TAG, "Unknown app installs blocked")

            // Set password requirements
            dpm.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC)
            dpm.setPasswordMinimumLength(componentName, 4)
            Log.d(TAG, "Password policy set")

            // Disable USB file transfers
            try {
                dpm.setSecureSettingEnabled(componentName, "adb_enabled", false)
                Log.d(TAG, "USB debugging disabled")
            } catch (e: Exception) {
                Log.w(TAG, "Could not disable USB debugging", e)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in device owner policies", e)
        }
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun isDeviceAdmin(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = DeviceAdminReceiver.getComponentName(context)
        return dpm.isAdminActive(componentName)
    }

    fun enableKioskMode(context: Context, packageName: String) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.setLockTaskPackages(
                    DeviceAdminReceiver.getComponentName(context),
                    arrayOf(packageName)
                )
                Log.d(TAG, "Kiosk mode enabled for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Error enabling kiosk mode", e)
            }
        }
    }

    fun disableKioskMode(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.setLockTaskPackages(
                    DeviceAdminReceiver.getComponentName(context),
                    arrayOf()
                )
                Log.d(TAG, "Kiosk mode disabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error disabling kiosk mode", e)
            }
        }
    }

    fun lockDevice(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = DeviceAdminReceiver.getComponentName(context)
        if (dpm.isAdminActive(componentName)) {
            try {
                dpm.lockNow()
                Log.d(TAG, "Device locked")
            } catch (e: Exception) {
                Log.e(TAG, "Error locking device", e)
            }
        }
    }

    fun wipeDevice(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                dpm.wipeData(0)
                Log.d(TAG, "Device wiped")
            } catch (e: Exception) {
                Log.e(TAG, "Error wiping device", e)
            }
        }
    }
}
