package com.example.wakeonlan

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection

class UpdateChecker(
    private val context: Context,
    private val networkService: NetworkService = DefaultNetworkService()
) {
    private val TAG = "UpdateChecker"
    private val GITHUB_API_URL = "https://api.github.com/repos/happyjake/WakeOnLAN/releases"
    
    /**
     * Check if there's a newer version available on GitHub
     * @return UpdateInfo with update details or null if no update available
     */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // Get current app version
            val currentVersion = getCurrentVersion()
            
            // Fetch the latest release from GitHub
            val response = networkService.get(GITHUB_API_URL)
            
            if (response.isSuccessful && response.body != null) {
                val releases = JSONArray(response.body)
                
                // Find the latest release
                if (releases.length() > 0) {
                    val latestRelease = releases.getJSONObject(0)
                    val tagName = latestRelease.getString("tag_name").removePrefix("v")
                    val releaseUrl = latestRelease.getString("html_url")
                    
                    // Find APK asset URL
                    var apkDownloadUrl: String? = null
                    val assets = latestRelease.getJSONArray("assets")
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val assetName = asset.getString("name")
                        if (assetName.endsWith(".apk")) {
                            apkDownloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                    
                    // Compare versions
                    if (isNewerVersion(currentVersion, tagName) && apkDownloadUrl != null) {
                        return@withContext UpdateInfo(
                            currentVersion = currentVersion,
                            newVersion = tagName,
                            releaseUrl = releaseUrl,
                            downloadUrl = apkDownloadUrl
                        )
                    }
                }
            } else {
                Log.e(TAG, "Error checking for updates: HTTP ${response.code}")
            }
            
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            return@withContext null
        }
    }
    
    /**
     * Download the APK file and return the local file
     */
    suspend fun downloadUpdate(downloadUrl: String): File? = withContext(Dispatchers.IO) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)
            val fileName = "WakeOnLAN_update.apk"
            val request = DownloadManager.Request(uri).apply {
                setTitle("WakeOnLAN Update")
                setDescription("Downloading latest version")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            }
            
            val downloadId = downloadManager.enqueue(request)
            var downloading = true
            var apkFile: File? = null
            
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                            
                            // Convert content:// URI to file
                            apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            Log.e(TAG, "Download failed")
                        }
                    }
                }
                
                cursor.close()
                if (downloading) {
                    // Wait a bit before checking again
                    withContext(Dispatchers.IO) {
                        Thread.sleep(500)
                    }
                }
            }
            
            return@withContext apkFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update", e)
            return@withContext null
        }
    }
    
    /**
     * Install the downloaded APK
     */
    fun installUpdate(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // For Android N and above, we need to use FileProvider
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing update", e)
        }
    }
    
    /**
     * Get the current app version
     */
    private fun getCurrentVersion(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                packageInfo.versionName
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Error getting app version", e)
            "0.0.0"
        }
    }
    
    /**
     * Compare version strings to determine if newVersion is newer than currentVersion
     */
    private fun isNewerVersion(currentVersion: String, newVersion: String): Boolean {
        val current = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val new = newVersion.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until minOf(current.size, new.size)) {
            if (new[i] > current[i]) return true
            if (new[i] < current[i]) return false
        }
        
        // If we get here, the common parts are equal, so the longer one is newer
        return new.size > current.size
    }
    
    /**
     * Data class to hold update information
     */
    data class UpdateInfo(
        val currentVersion: String,
        val newVersion: String,
        val releaseUrl: String,
        val downloadUrl: String
    )
} 