package com.emishield.locker.repository

import android.util.Log
import com.emishield.locker.api.ApiService
import com.emishield.locker.api.RetrofitClient
import com.emishield.locker.model.DeviceRegistrationRequest
import com.emishield.locker.model.EmiStatusResponse
import com.emishield.locker.model.PaymentUpdateRequest

/**
 * Repository for all EMI related API calls
 */
class EmiRepository {

    private val apiService: ApiService = RetrofitClient.getApiService()
    private val TAG = "EmiRepository"

    suspend fun registerDevice(deviceId: String, model: String): Result<String> {
        return try {
            val request = DeviceRegistrationRequest(
                deviceId = deviceId,
                model = model
            )
            val response = apiService.registerDevice(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success("Device registered successfully")
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device", e)
            Result.failure(e)
        }
    }

    suspend fun getEmiStatus(deviceId: String): Result<EmiStatusResponse> {
        return try {
            val response = apiService.getEmiStatus(deviceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get EMI status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching EMI status", e)
            Result.failure(e)
        }
    }

    suspend fun updatePayment(
        deviceId: String,
        amount: Double,
        transactionId: String
    ): Result<String> {
        return try {
            val request = PaymentUpdateRequest(
                deviceId = deviceId,
                amount = amount,
                transactionId = transactionId
            )
            val response = apiService.updatePayment(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success("Payment updated successfully")
            } else {
                Result.failure(Exception("Payment update failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating payment", e)
            Result.failure(e)
        }
    }

    suspend fun logEvent(deviceId: String, eventType: String, data: String): Result<String> {
        return try {
            val response = apiService.logDeviceEvent(deviceId, eventType, data)
            if (response.isSuccessful) {
                Result.success("Event logged")
            } else {
                Result.failure(Exception("Log failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging event", e)
            Result.failure(e)
        }
    }
}
