package com.example.wakeonlan

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class UpdateCheckerTest {

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockPackageManager: PackageManager

    @MockK
    private lateinit var mockDownloadManager: DownloadManager

    @MockK
    private lateinit var mockConnection: HttpURLConnection

    @MockK
    private lateinit var mockUrl: URL

    private lateinit var updateChecker: UpdateChecker
    
    private val packageName = "com.example.wakeonlan"
    private val currentVersion = "1.0.2"
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        
        // Set up package manager mock
        every { mockContext.packageManager } returns mockPackageManager
        every { mockContext.packageName } returns packageName
        
        val packageInfo = PackageInfo().apply {
            versionName = currentVersion
        }
        every { mockPackageManager.getPackageInfo(packageName, 0) } returns packageInfo
        
        // Set up download manager mock
        every { mockContext.getSystemService(Context.DOWNLOAD_SERVICE) } returns mockDownloadManager
        
        // Create UpdateChecker with mocked context
        updateChecker = UpdateChecker(mockContext)
        
        // Mock URL constructor and connection
        mockkConstructor(URL::class)
        every { anyConstructed<URL>().openConnection() } returns mockConnection
        every { mockConnection.requestMethod = any() } returns Unit
        every { mockConnection.setRequestProperty(any(), any()) } returns Unit
        every { mockConnection.connectTimeout = any() } returns Unit
        every { mockConnection.readTimeout = any() } returns Unit
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `isNewerVersion correctly compares versions`() {
        // Use reflection to access private method
        val isNewerVersionMethod = UpdateChecker::class.java.getDeclaredMethod(
            "isNewerVersion",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        // Test cases where new version is newer
        assertTrue(isNewerVersionMethod.invoke(updateChecker, "1.0.0", "1.0.1") as Boolean)
        assertTrue(isNewerVersionMethod.invoke(updateChecker, "1.0.0", "1.1.0") as Boolean)
        assertTrue(isNewerVersionMethod.invoke(updateChecker, "1.0.0", "2.0.0") as Boolean)
        assertTrue(isNewerVersionMethod.invoke(updateChecker, "1.0", "1.0.1") as Boolean)
        
        // Test cases where new version is not newer
        assertFalse(isNewerVersionMethod.invoke(updateChecker, "1.0.1", "1.0.0") as Boolean)
        assertFalse(isNewerVersionMethod.invoke(updateChecker, "1.1.0", "1.0.0") as Boolean)
        assertFalse(isNewerVersionMethod.invoke(updateChecker, "2.0.0", "1.0.0") as Boolean)
        assertFalse(isNewerVersionMethod.invoke(updateChecker, "1.0.1", "1.0") as Boolean)
        
        // Test cases where versions are equal
        assertFalse(isNewerVersionMethod.invoke(updateChecker, "1.0.0", "1.0.0") as Boolean)
    }

    @Test
    fun `getCurrentVersion returns correct version`() {
        // Use reflection to access private method
        val getCurrentVersionMethod = UpdateChecker::class.java.getDeclaredMethod(
            "getCurrentVersion"
        ).apply { isAccessible = true }
        
        // Test that getCurrentVersion returns the mocked version
        assertEquals(currentVersion, getCurrentVersionMethod.invoke(updateChecker))
    }
    
    @Test
    fun `checkForUpdate returns null when no updates available`() = runTest {
        // Mock connection response with same version as current
        val jsonResponse = createMockReleaseJson(currentVersion)
        mockConnectionResponse(HttpURLConnection.HTTP_OK, jsonResponse)
        
        // Call the method under test
        val result = updateChecker.checkForUpdate()
        
        // Verify result is null (no update available)
        assertNull(result)
    }
    
    @Test
    fun `checkForUpdate returns UpdateInfo when newer version available`() = runTest {
        // Mock connection response with newer version
        val newerVersion = "1.0.3"
        val jsonResponse = createMockReleaseJson(newerVersion)
        mockConnectionResponse(HttpURLConnection.HTTP_OK, jsonResponse)
        
        // Call the method under test
        val result = updateChecker.checkForUpdate()
        
        // Verify result contains correct update info
        assertNotNull(result)
        assertEquals(currentVersion, result?.currentVersion)
        assertEquals(newerVersion, result?.newVersion)
        assertEquals("https://github.com/happyjake/WakeOnLAN/releases/tag/v$newerVersion", result?.releaseUrl)
        assertEquals("https://github.com/happyjake/WakeOnLAN/releases/download/v$newerVersion/app-release.apk", result?.downloadUrl)
    }
    
    @Test
    fun `checkForUpdate returns null when connection fails`() = runTest {
        // Mock connection failure
        mockConnectionResponse(HttpURLConnection.HTTP_NOT_FOUND, "")
        
        // Call the method under test
        val result = updateChecker.checkForUpdate()
        
        // Verify result is null (connection failed)
        assertNull(result)
    }
    
    @Test
    fun `checkForUpdate returns null when no APK asset found`() = runTest {
        // Mock connection response with no APK asset
        val jsonResponse = createMockReleaseJsonNoApk("1.0.3")
        mockConnectionResponse(HttpURLConnection.HTTP_OK, jsonResponse)
        
        // Call the method under test
        val result = updateChecker.checkForUpdate()
        
        // Verify result is null (no APK found)
        assertNull(result)
    }
    
    @Test
    fun `downloadUpdate returns File when download successful`() = runTest {
        // Mock download manager to simulate successful download
        val downloadUrl = "https://example.com/app-release.apk"
        val downloadId = 123L
        val downloadFileName = "WakeOnLAN_update.apk"
        
        // Mock the download request enqueue
        every { mockDownloadManager.enqueue(any()) } returns downloadId
        
        // Create a successful download cursor
        val successfulCursor = createMockDownloadCursor(
            status = DownloadManager.STATUS_SUCCESSFUL,
            localUri = "file:///mnt/sdcard/Android/data/com.example.wakeonlan/files/Download/$downloadFileName"
        )
        
        // Return the cursor when query is called
        every { mockDownloadManager.query(any()) } returns successfulCursor
        
        // Mock the external files dir
        val mockExternalDir = File("/mnt/sdcard/Android/data/com.example.wakeonlan/files")
        every { mockContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) } returns mockExternalDir
        
        // Call the method under test
        val result = updateChecker.downloadUpdate(downloadUrl)
        
        // Verify result is a file pointing to the downloaded file
        assertNotNull(result)
        assertEquals("$mockExternalDir/$downloadFileName", result?.absolutePath)
    }
    
    @Test
    fun `downloadUpdate returns null when download fails`() = runTest {
        // Mock download manager to simulate failed download
        val downloadUrl = "https://example.com/app-release.apk"
        val downloadId = 123L
        
        // Mock the download request enqueue
        every { mockDownloadManager.enqueue(any()) } returns downloadId
        
        // Create a failed download cursor
        val failedCursor = createMockDownloadCursor(status = DownloadManager.STATUS_FAILED)
        
        // Return the cursor when query is called
        every { mockDownloadManager.query(any()) } returns failedCursor
        
        // Call the method under test
        val result = updateChecker.downloadUpdate(downloadUrl)
        
        // Verify result is null (download failed)
        assertNull(result)
    }
    
    private fun mockConnectionResponse(responseCode: Int, response: String) {
        every { mockConnection.responseCode } returns responseCode
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = ByteArrayInputStream(response.toByteArray())
            every { mockConnection.inputStream } returns inputStream
        }
    }
    
    private fun createMockReleaseJson(version: String): String {
        val release = JSONObject().apply {
            put("tag_name", "v$version")
            put("html_url", "https://github.com/happyjake/WakeOnLAN/releases/tag/v$version")
            
            val assets = JSONArray()
            val asset = JSONObject().apply {
                put("name", "app-release.apk")
                put("browser_download_url", "https://github.com/happyjake/WakeOnLAN/releases/download/v$version/app-release.apk")
            }
            assets.put(asset)
            
            put("assets", assets)
        }
        
        val releases = JSONArray()
        releases.put(release)
        
        return releases.toString()
    }
    
    private fun createMockReleaseJsonNoApk(version: String): String {
        val release = JSONObject().apply {
            put("tag_name", "v$version")
            put("html_url", "https://github.com/happyjake/WakeOnLAN/releases/tag/v$version")
            
            val assets = JSONArray()
            val asset = JSONObject().apply {
                put("name", "source.zip")
                put("browser_download_url", "https://github.com/happyjake/WakeOnLAN/releases/download/v$version/source.zip")
            }
            assets.put(asset)
            
            put("assets", assets)
        }
        
        val releases = JSONArray()
        releases.put(release)
        
        return releases.toString()
    }
    
    private fun createMockDownloadCursor(
        status: Int, 
        localUri: String? = null
    ): Cursor {
        val matrixCursor = MatrixCursor(arrayOf(
            DownloadManager.COLUMN_STATUS,
            DownloadManager.COLUMN_LOCAL_URI
        ))
        
        val rowData = mutableListOf<Any>(status)
        if (localUri != null) {
            rowData.add(localUri)
        } else {
            rowData.add("")
        }
        
        matrixCursor.addRow(rowData)
        return matrixCursor
    }
} 