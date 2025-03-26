package com.example.wakeonlan

/**
 * Interface to abstract network operations for better testability
 */
interface NetworkService {
    /**
     * Perform a GET request and return the response as a string
     * @param url The URL to fetch
     * @return Response body as a string or null if the request failed
     */
    suspend fun get(url: String): NetworkResponse
    
    /**
     * Data class to represent a network response
     */
    data class NetworkResponse(
        val isSuccessful: Boolean,
        val body: String? = null,
        val code: Int = 0
    )
} 