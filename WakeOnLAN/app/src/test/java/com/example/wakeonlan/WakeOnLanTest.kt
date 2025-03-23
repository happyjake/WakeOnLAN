package com.example.wakeonlan

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WakeOnLanTest {

    @Test
    fun testMacAddressValidation() {
        val wakeOnLan = WakeOnLan()
        
        // Valid formats
        assertTrue(wakeOnLan.isValidMacAddress("00:11:22:33:44:55"))
        assertTrue(wakeOnLan.isValidMacAddress("00-11-22-33-44-55"))
        assertTrue(wakeOnLan.isValidMacAddress("001122334455"))
        
        // Invalid formats
        assertFalse(wakeOnLan.isValidMacAddress(""))
        assertFalse(wakeOnLan.isValidMacAddress("00:11:22:33:44"))         // Too short
        assertFalse(wakeOnLan.isValidMacAddress("00:11:22:33:44:55:66"))   // Too long
        assertFalse(wakeOnLan.isValidMacAddress("00:11:GG:33:44:55"))      // Invalid characters
    }
    
    @Test
    fun testCleanMacAddress() {
        val wakeOnLan = WakeOnLan()
        
        assertEquals("001122334455", wakeOnLan.cleanMacAddress("00:11:22:33:44:55"))
        assertEquals("001122334455", wakeOnLan.cleanMacAddress("00-11-22-33-44-55"))
        assertEquals("001122334455", wakeOnLan.cleanMacAddress("001122334455"))
    }
    
    @Test
    fun testCreateMagicPacket() {
        val wakeOnLan = WakeOnLan()
        val macAddress = "00:11:22:33:44:55"
        val magicPacket = wakeOnLan.createMagicPacket(macAddress)
        
        // Magic packet should be 102 bytes: 6 bytes of 0xFF + 16 repetitions of 6-byte MAC
        assertEquals(102, magicPacket.size)
        
        // First 6 bytes should be 0xFF
        for (i in 0 until 6) {
            assertEquals(0xFF.toByte(), magicPacket[i])
        }
        
        // Check first byte of first MAC repetition
        assertEquals(0x00.toByte(), magicPacket[6])
        
        // Check first byte of last MAC repetition
        assertEquals(0x00.toByte(), magicPacket[6 + 15 * 6])
    }
    
    @Test
    fun testWakeOnLanSendsCorrectPacket() {
        // Mock socket and packet
        val mockSocket = mock<DatagramSocket>()
        val socketFactory = object : WakeOnLan.SocketFactory {
            override fun createSocket(): DatagramSocket = mockSocket
        }
        
        val wakeOnLan = WakeOnLan(socketFactory)
        val macAddress = "00:11:22:33:44:55"
        val broadcastIp = "192.168.1.255"
        
        // Capture the packet
        val packetCaptor = argumentCaptor<DatagramPacket>()
        
        // Call the method
        wakeOnLan.sendWakeOnLan(macAddress, broadcastIp)
        
        // Verify the packet
        verify(mockSocket).send(packetCaptor.capture())
        verify(mockSocket).close()
        
        val capturedPacket = packetCaptor.firstValue
        
        // Verify packet properties
        assertEquals(102, capturedPacket.length)  // 6 bytes of FF + 16 repetitions of 6-byte MAC
        assertEquals(9, capturedPacket.port)      // WOL uses port 9
        assertEquals(broadcastIp, capturedPacket.address.hostAddress)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testInvalidMacAddressThrowsException() {
        val wakeOnLan = WakeOnLan()
        wakeOnLan.sendWakeOnLan("invalid:mac:address", "192.168.1.255")
    }
} 