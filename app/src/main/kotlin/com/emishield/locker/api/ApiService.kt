package com.emishield.locker.api

import com.emishield.locker.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service Interface for EMI Locker
 */
interface ApiService {

    /**
     * Register device with backend
     */
    @POST("/device/register")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>

    /**
     * Get EMI status for a device
     */
    @GET("/emi/status")
    suspend fun getEmiStatus(
        @Query("device_id") deviceId: String
    ): Response<EmiStatusResponse>

    /**
     * Update payment status
     */
    @POST("/payment/update")
    suspend fun updatePayment(
        @Body request: PaymentUpdateRequest
    ): Response<ApiResponse<String>>

    /**
     * Get device policy configuration
     */
    @GET("/device/policy")
    suspend fun getDevicePolicy(
        @Query("device_id") deviceId: String
    ): Response<ApiResponse<Map<String, Any>>>

    /**
     * Log device event
     */
    @POST("/device/log-event")
    suspend fun logDeviceEvent(
        @Query("device_id") deviceId: String,
        @Query("event_type") eventType: String,
        @Query("data") data: String
    ): Response<ApiResponse<String>>
}
