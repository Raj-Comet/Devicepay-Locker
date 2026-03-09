package com.emishield.locker.model

import com.google.gson.annotations.SerializedName

/**
 * EMI Status Response from backend API
 */
data class EmiStatusResponse(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("status")
    val status: String, // PAID or UNPAID
    @SerializedName("due_days")
    val dueDays: Int,
    @SerializedName("amount")
    val amount: Double = 0.0,
    @SerializedName("next_due_date")
    val nextDueDate: String = "",
    @SerializedName("message")
    val message: String = ""
) {
    fun isPaid(): Boolean = status.equals("PAID", ignoreCase = true)
}

/**
 * Device Registration Payload
 */
data class DeviceRegistrationRequest(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("manufacturer")
    val manufacturer: String = android.os.Build.MANUFACTURER,
    @SerializedName("android_version")
    val androidVersion: String = android.os.Build.VERSION.RELEASE,
    @SerializedName("app_version")
    val appVersion: String = "1.0.0"
)

/**
 * Device Registration Response
 */
data class DeviceRegistrationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("message")
    val message: String
)

/**
 * Payment Update Request
 */
data class PaymentUpdateRequest(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("transaction_id")
    val transactionId: String
)

/**
 * API Response Wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String = ""
)
