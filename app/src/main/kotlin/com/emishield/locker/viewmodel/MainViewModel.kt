package com.emishield.locker.viewmodel

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.emishield.locker.model.EmiStatusResponse
import com.emishield.locker.repository.EmiRepository
import com.emishield.locker.utils.DevicePolicyUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Main Activity
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MainViewModel"
    private val repository = EmiRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _emiStatus = MutableLiveData<EmiStatusResponse?>(null)
    val emiStatus: LiveData<EmiStatusResponse?> = _emiStatus

    private val _isDeviceOwner = MutableLiveData<Boolean>()
    val isDeviceOwner: LiveData<Boolean> = _isDeviceOwner

    private val _isDeviceAdmin = MutableLiveData<Boolean>()
    val isDeviceAdmin: LiveData<Boolean> = _isDeviceAdmin

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        checkDeviceStatus()
        registerDevice()
        checkEmiStatus()
    }

    fun checkDeviceStatus() {
        val isOwner = DevicePolicyUtils.isDeviceOwner(getApplication())
        val isAdmin = DevicePolicyUtils.isDeviceAdmin(getApplication())
        _isDeviceOwner.value = isOwner
        _isDeviceAdmin.value = isAdmin
        Log.d(TAG, "Device Owner: $isOwner, Device Admin: $isAdmin")
    }

    fun registerDevice() {
        _loading.value = true
        scope.launch {
            val deviceId = getDeviceId()
            val model = android.os.Build.MODEL
            val result = repository.registerDevice(deviceId, model)
            
            result.onSuccess {
                _loading.value = false
                Log.d(TAG, "Device registered successfully")
            }
            result.onFailure { error ->
                _loading.value = false
                _error.value = "Registration failed: ${error.message}"
                Log.e(TAG, "Registration error", error)
            }
        }
    }

    fun checkEmiStatus() {
        _loading.value = true
        scope.launch {
            val deviceId = getDeviceId()
            val result = repository.getEmiStatus(deviceId)
            
            result.onSuccess { status ->
                _loading.value = false
                _emiStatus.value = status
                Log.d(TAG, "EMI Status: ${status.status}")
            }
            result.onFailure { error ->
                _loading.value = false
                _error.value = "Failed to fetch EMI status: ${error.message}"
                Log.e(TAG, "EMI status error", error)
            }
        }
    }

    fun updatePayment(amount: Double, transactionId: String) {
        _loading.value = true
        scope.launch {
            val deviceId = getDeviceId()
            val result = repository.updatePayment(deviceId, amount, transactionId)
            
            result.onSuccess {
                _loading.value = false
                checkEmiStatus() // Refresh status
            }
            result.onFailure { error ->
                _loading.value = false
                _error.value = "Payment update failed: ${error.message}"
                Log.e(TAG, "Payment error", error)
            }
        }
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun clearError() {
        _error.value = null
    }
}
