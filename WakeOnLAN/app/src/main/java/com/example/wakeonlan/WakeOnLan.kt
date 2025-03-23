package com.example.wakeonlan

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * A utility class for sending Wake-on-LAN magic packets.
 */
class WakeOnLan(private val socketFactory: SocketFactory = DefaultSocketFactory()) {

    interface SocketFactory {
        fun createSocket(): DatagramSocket
    }

    class DefaultSocketFactory : SocketFactory {
        override fun createSocket(): DatagramSocket = DatagramSocket()
    }

    /**
     * Validates if the given string is a valid MAC address.
     * Accepted formats: 00:11:22:33:44:55, 00-11-22-33-44-55, 001122334455
     */
    fun isValidMacAddress(macAddress: String): Boolean {
        if (macAddress.isEmpty()) return false

        // Format with colons or hyphens
        val regex1 = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        // Format without separators
        val regex2 = Regex("^([0-9A-Fa-f]{12})$")

        return regex1.matches(macAddress) || regex2.matches(macAddress)
    }

    /**
     * Removes any separators from the MAC address and returns a clean string.
     */
    fun cleanMacAddress(macAddress: String): String {
        return macAddress.replace(Regex("[:-]"), "")
    }

    /**
     * Creates a magic packet for the given MAC address.
     * A magic packet consists of 6 bytes of 0xFF followed by 16 repetitions of the MAC address.
     */
    fun createMagicPacket(macAddress: String): ByteArray {
        if (!isValidMacAddress(macAddress)) {
            throw IllegalArgumentException("Invalid MAC address format")
        }

        val cleanMac = cleanMacAddress(macAddress)
        val macBytes = ByteArray(6)

        // Convert MAC string to bytes
        for (i in 0 until 6) {
            val byteStr = cleanMac.substring(i * 2, i * 2 + 2)
            macBytes[i] = byteStr.toInt(16).toByte()
        }

        // Create magic packet (6 bytes of 0xFF followed by MAC repeated 16 times)
        val packet = ByteArray(6 + 16 * macBytes.size)
        
        // Fill first 6 bytes with 0xFF
        for (i in 0 until 6) {
            packet[i] = 0xFF.toByte()
        }
        
        // Add 16 repetitions of the MAC address
        for (i in 0 until 16) {
            System.arraycopy(macBytes, 0, packet, 6 + i * macBytes.size, macBytes.size)
        }
        
        return packet
    }

    /**
     * Sends a Wake-on-LAN magic packet to the specified MAC address.
     *
     * @param macAddress The MAC address of the target device
     * @param broadcastIp The broadcast IP address of the network (typically x.x.x.255)
     * @throws IllegalArgumentException if the MAC address format is invalid
     */
    fun sendWakeOnLan(macAddress: String, broadcastIp: String): Boolean {
        try {
            val packet = createMagicPacket(macAddress)
            val address = InetAddress.getByName(broadcastIp)
            val datagramPacket = DatagramPacket(packet, packet.size, address, 9) // Port 9 is standard for WOL
            
            val socket = socketFactory.createSocket()
            socket.use { s ->
                s.broadcast = true
                s.send(datagramPacket)
            }
            
            return true
        } catch (e: Exception) {
            if (e is IllegalArgumentException) {
                throw e // Re-throw validation errors
            }
            return false
        }
    }
} 