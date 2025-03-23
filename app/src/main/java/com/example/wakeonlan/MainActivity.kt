package com.example.wakeonlan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.wakeonlan.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val wakeOnLan = WakeOnLan()
    private lateinit var updateChecker: UpdateChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize update checker
        updateChecker = UpdateChecker(this)

        setupListeners()
        
        // Check for intent extras and automatically wake if parameters are provided
        intent?.let { intent ->
            val macAddress = intent.getStringExtra("mac")
            val broadcastIp = intent.getStringExtra("ip")
            
            if (!macAddress.isNullOrEmpty() && !broadcastIp.isNullOrEmpty()) {
                // Set the values in the UI
                binding.editTextMacAddress.setText(macAddress)
                binding.editTextBroadcastIp.setText(broadcastIp)
                
                // Automatically trigger wake if parameters are valid
                if (wakeOnLan.isValidMacAddress(macAddress)) {
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
                } else {
                    Toast.makeText(this, getString(R.string.error_invalid_mac), Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Check for updates
        checkForUpdates()
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
    
    /**
     * Check for updates from GitHub releases
     */
    private fun checkForUpdates() {
        lifecycleScope.launch {
            val updateInfo = updateChecker.checkForUpdate()
            
            updateInfo?.let { info ->
                // Show update dialog
                showUpdateAvailableDialog(info)
            }
        }
    }
    
    /**
     * Show a dialog informing the user about the available update
     */
    private fun showUpdateAvailableDialog(updateInfo: UpdateChecker.UpdateInfo) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_available_title))
            .setMessage(getString(
                R.string.update_available_message,
                updateInfo.newVersion,
                updateInfo.currentVersion
            ))
            .setPositiveButton(getString(R.string.update_button_download)) { _, _ ->
                downloadUpdate(updateInfo.downloadUrl)
            }
            .setNegativeButton(getString(R.string.update_button_cancel), null)
            .show()
    }
    
    /**
     * Download the update APK
     */
    private fun downloadUpdate(downloadUrl: String) {
        Toast.makeText(this, getString(R.string.update_downloading), Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            val apkFile = updateChecker.downloadUpdate(downloadUrl)
            
            if (apkFile != null) {
                Toast.makeText(this@MainActivity, getString(R.string.update_download_complete), Toast.LENGTH_SHORT).show()
                showInstallUpdateDialog(apkFile)
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.update_download_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Show a dialog asking the user if they want to install the update
     */
    private fun showInstallUpdateDialog(apkFile: java.io.File) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_install_title))
            .setMessage(getString(R.string.update_install_message))
            .setPositiveButton(getString(R.string.update_button_install)) { _, _ ->
                updateChecker.installUpdate(apkFile)
            }
            .setNegativeButton(getString(R.string.update_button_later), null)
            .show()
    }
} 