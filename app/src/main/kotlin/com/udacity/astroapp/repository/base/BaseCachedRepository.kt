package com.udacity.astroapp.repository.base

import android.util.Log
import com.udacity.astroapp.cache.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseCachedRepository(protected val cacheManager: CacheManager) {

    protected fun isCacheValid(timestamp: Long, cacheDurationMillis: Long): Boolean {
        return System.currentTimeMillis() - timestamp < cacheDurationMillis
    }

    protected fun isDatabaseCorruption(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("corrupt") ||
            message.contains("malformed") ||
            message.contains("database disk image is malformed") ||
            exception is android.database.sqlite.SQLiteDatabaseCorruptException
    }

    protected suspend fun <T> handleCacheOperation(
        repositoryName: String,
        operation: String,
        cacheOperation: suspend () -> T?,
        networkOperation: suspend () -> T?,
        fallbackOperation: suspend () -> T? = { null }
    ): T? {
        return withContext(Dispatchers.IO) {
            try {
                // Try cache first
                val cachedResult = cacheOperation()
                if (cachedResult != null) {
                    return@withContext cachedResult
                }

                // Fall back to network
                networkOperation()
            } catch (e: Exception) {
                Log.e(repositoryName, "Error in $operation", e)

                // Check if it's a database corruption issue
                if (isDatabaseCorruption(e)) {
                    Log.w(repositoryName, "Database corruption detected, attempting recovery")
                    if (cacheManager.handleDatabaseCorruption()) {
                        // Retry network operation after recovery
                        try {
                            networkOperation()
                        } catch (retryException: Exception) {
                            Log.e(repositoryName, "Retry after recovery failed", retryException)
                            fallbackOperation()
                        }
                    } else {
                        fallbackOperation()
                    }
                } else {
                    // Try fallback operation (usually cached data)
                    try {
                        fallbackOperation()
                    } catch (fallbackException: Exception) {
                        Log.e(repositoryName, "Fallback operation failed", fallbackException)
                        null
                    }
                }
            }
        }
    }

    protected fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}
