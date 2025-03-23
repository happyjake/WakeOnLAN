package com.example.wakeonlan

import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.lang.reflect.Field

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class MainActivityTest {

    private lateinit var mockWakeOnLan: WakeOnLan
    
    @Before
    fun setup() {
        mockWakeOnLan = mock()
    }
    
    @Test
    fun testWakeButtonClick_validMac_callsWakeOnLan() {
        // Given
        whenever(mockWakeOnLan.isValidMacAddress("00:11:22:33:44:55")).thenReturn(true)
        whenever(mockWakeOnLan.sendWakeOnLan("00:11:22:33:44:55", "192.168.1.255")).thenReturn(true)
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.onActivity { activity ->
                // Replace real WakeOnLan with mock
                setMockWakeOnLan(activity, mockWakeOnLan)
                
                // When
                val macEditText = activity.findViewById<EditText>(R.id.editTextMacAddress)
                val broadcastEditText = activity.findViewById<EditText>(R.id.editTextBroadcastIp)
                val wakeButton = activity.findViewById<Button>(R.id.buttonWake)
                
                macEditText.setText("00:11:22:33:44:55")
                broadcastEditText.setText("192.168.1.255")
                wakeButton.performClick()
                
                // Need to run any pending coroutines
                shadowOf(activity.mainLooper).idle()
                
                // Then
                verify(mockWakeOnLan).isValidMacAddress("00:11:22:33:44:55")
                verify(mockWakeOnLan).sendWakeOnLan("00:11:22:33:44:55", "192.168.1.255")
            }
        }
    }
    
    @Test
    fun testWakeButtonClick_invalidMac_doesNotCallWakeOnLan() {
        // Given
        whenever(mockWakeOnLan.isValidMacAddress("invalid-mac")).thenReturn(false)
        
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.onActivity { activity ->
                // Replace real WakeOnLan with mock
                setMockWakeOnLan(activity, mockWakeOnLan)
                
                // When
                val macEditText = activity.findViewById<EditText>(R.id.editTextMacAddress)
                val wakeButton = activity.findViewById<Button>(R.id.buttonWake)
                
                macEditText.setText("invalid-mac")
                wakeButton.performClick()
                
                // Then
                verify(mockWakeOnLan).isValidMacAddress("invalid-mac")
                verify(mockWakeOnLan, never()).sendWakeOnLan("invalid-mac", "192.168.1.255")
            }
        }
    }
    
    // Helper method to replace the real WakeOnLan instance with our mock
    private fun setMockWakeOnLan(activity: MainActivity, mockWakeOnLan: WakeOnLan) {
        val field: Field = MainActivity::class.java.getDeclaredField("wakeOnLan")
        field.isAccessible = true
        field.set(activity, mockWakeOnLan)
    }
} 