package com.emishield.locker.locker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.emishield.locker.R

/**
 * Lock Screen Activity - Displayed when EMI payment is pending
 * This activity cannot be exited by user
 */
class LockActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var dueTextView: TextView
    private lateinit var payButton: Button
    private lateinit var supportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        // Get EMI status from intent
        val dueDays = intent.getIntExtra("due_days", 0)
        val message = intent.getStringExtra("message") ?: getString(R.string.lock_message)

        initializeViews()
        setupClickListeners(dueDays)
        
        // Log lock event
        LockScreenManager.logLockEvent(this, dueDays)
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.tvLockTitle)
        messageTextView = findViewById(R.id.tvLockMessage)
        dueTextView = findViewById(R.id.tvDueDays)
        payButton = findViewById(R.id.btnPayNow)
        supportButton = findViewById(R.id.btnContactSupport)

        titleTextView.text = getString(R.string.lock_title)
        messageTextView.text = intent.getStringExtra("message") ?: getString(R.string.lock_message)
        dueTextView.text = String.format(getString(R.string.due_days), intent.getIntExtra("due_days", 0))
    }

    private fun setupClickListeners(dueDays: Int) {
        payButton.setOnClickListener {
            openPaymentURL()
        }

        supportButton.setOnClickListener {
            contactSupport()
        }
    }

    private fun openPaymentURL() {
        try {
            val paymentURL = "https://payment.example.com/emi"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(paymentURL)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun contactSupport() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+1234567890")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Override back button - cannot exit lock screen
     */
    override fun onBackPressed() {
        // Do nothing - user cannot exit lock screen
    }

    /**
     * Override key events to prevent navigation
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH -> return true // Prevent these keys
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Prevent volume button navigation
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> return true
        }
        return super.onKeyUp(keyCode, event)
    }

    /**
     * Keep activity in foreground
     */
    override fun onPause() {
        super.onPause()
        // Restart activity if paused
        val intent = Intent(this, LockActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
    }
}
