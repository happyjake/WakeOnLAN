package com.example.wakeonlan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Default implementation of NetworkService using URL and HttpURLConnection
 */
class DefaultNetworkService : NetworkService {
    
    override suspend fun get(url: String): NetworkService.NetworkResponse = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val responseCode = connection.responseCode
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                NetworkService.NetworkResponse(
                    isSuccessful = true,
                    body = response,
                    code = responseCode
                )
            } else {
                NetworkService.NetworkResponse(
                    isSuccessful = false,
                    code = responseCode
                )
            }
        } catch (e: Exception) {
            NetworkService.NetworkResponse(
                isSuccessful = false,
                code = -1
            )
        }
    }
} 