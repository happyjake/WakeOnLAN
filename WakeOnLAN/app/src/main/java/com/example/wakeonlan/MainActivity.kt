package com.example.wakeonlan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.wakeonlan.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val wakeOnLan = WakeOnLan()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonWake.setOnClickListener {
            val macAddress = binding.editTextMacAddress.text.toString().trim()
            val broadcastIp = binding.editTextBroadcastIp.text.toString().trim()

            if (!wakeOnLan.isValidMacAddress(macAddress)) {
                Toast.makeText(this, getString(R.string.error_invalid_mac), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Run on IO thread
            lifecycleScope.launch {
                val success = withContext(Dispatchers.IO) {
                    wakeOnLan.sendWakeOnLan(macAddress, broadcastIp)
                }

                // Show result on UI thread
                val message = if (success) {
                    getString(R.string.message_success)
                } else {
                    getString(R.string.message_error)
                }
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
} 