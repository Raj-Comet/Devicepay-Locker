package com.emishield.locker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.emishield.locker.service.EmiCheckWorker
import com.emishield.locker.service.EmiCheckService
import com.emishield.locker.utils.DevicePolicyUtils
import com.emishield.locker.viewmodel.MainViewModel
import java.util.concurrent.TimeUnit

/**
 * Main Activity - Entry point of the application
 */
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var viewModel: MainViewModel
    private lateinit var statusTextView: TextView
    private lateinit var deviceIdTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var registerButton: Button
    private lateinit var checkStatusButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "MainActivity created")

        // Initialize views
        initializeViews()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Apply device policies
        DevicePolicyUtils.applyDevicePolicies(this)

        // Setup observers
        setupObservers()

        // Setup click listeners
        setupClickListeners()

        // Start EMI check service
        startEmiService()

        // Schedule periodic EMI checks with WorkManager
        scheduleEmiCheckWorker()
    }

    private fun initializeViews() {
        statusTextView = findViewById(R.id.tvStatus)
        deviceIdTextView = findViewById(R.id.tvDeviceId)
        progressBar = findViewById(R.id.progressBar)
        registerButton = findViewById(R.id.btnRegister)
        checkStatusButton = findViewById(R.id.btnCheckStatus)
    }

    private fun setupObservers() {
        viewModel.isDeviceOwner.observe(this) { isOwner ->
            statusTextView.text = "Device Owner: $isOwner"
        }

        viewModel.isDeviceAdmin.observe(this) { isAdmin ->
            statusTextView.append("\nDevice Admin: $isAdmin")
        }

        viewModel.emiStatus.observe(this) { status ->
            if (status != null) {
                val statusText = "EMI Status: ${status.status}\nAmount: ₹${status.amount}\nDue Days: ${status.dueDays}"
                statusTextView.append("\n$statusText")
                Log.d(TAG, "EMI Status updated: ${status.status}")
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                statusTextView.append("\nError: $error")
                Log.e(TAG, "Error: $error")
            }
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            Log.d(TAG, "Register button clicked")
            viewModel.registerDevice()
        }

        checkStatusButton.setOnClickListener {
            Log.d(TAG, "Check status button clicked")
            viewModel.checkEmiStatus()
        }
    }

    private fun startEmiService() {
        val serviceIntent = Intent(this, EmiCheckService::class.java)
        startService(serviceIntent)
        Log.d(TAG, "EMI Check Service started")
    }

    private fun scheduleEmiCheckWorker() {
        val emiCheckRequest = PeriodicWorkRequestBuilder<EmiCheckWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "emi_check_work",
            ExistingPeriodicWorkPolicy.KEEP,
            emiCheckRequest
        )
        Log.d(TAG, "EMI Check Worker scheduled")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed")
    }
}
