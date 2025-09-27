package com.udacity.astroapp.cache

import android.content.Context
import android.util.Log
import com.udacity.astroapp.data.AppDatabase
import com.udacity.astroapp.utils.CacheFileUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CacheManager(private val context: Context) {

    suspend fun handleDatabaseCorruption(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.w(TAG, "Attempting database corruption recovery")

                // Close current database connection
                AppDatabase.databaseInstance?.close()

                // Delete corrupted database
                val databasePath = context.getDatabasePath("astroAppDatabase")
                val walPath = File(databasePath.path + "-wal")
                val shmPath = File(databasePath.path + "-shm")

                var recovered = true
                if (databasePath.exists() && !databasePath.delete()) {
                    Log.e(TAG, "Failed to delete corrupted database")
                    recovered = false
                }
                if (walPath.exists() && !walPath.delete()) {
                    Log.e(TAG, "Failed to delete WAL file")
                }
                if (shmPath.exists() && !shmPath.delete()) {
                    Log.e(TAG, "Failed to delete SHM file")
                }

                if (recovered) {
                    // Reset singleton instance
                    AppDatabase.databaseInstance = null
                    Log.i(TAG, "Database corruption recovery completed")
                }

                recovered
            } catch (e: Exception) {
                Log.e(TAG, "Database corruption recovery failed", e)
                false
            }
        }
    }

    suspend fun clearAllCaches(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Clearing all caches")

                val httpCleared =
                    CacheFileUtils.deleteCacheDirectory(
                        context,
                        CacheFileUtils.CacheDirectories.HTTP_CACHE
                    )
                val imageCleared =
                    CacheFileUtils.deleteCacheDirectory(
                        context,
                        CacheFileUtils.CacheDirectories.IMAGE_CACHE
                    )

                // Clear database would require DAO access, handle in repositories
                Log.i(TAG, "Cache clearing completed - HTTP: $httpCleared, Image: $imageCleared")
                httpCleared && imageCleared
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear caches", e)
                false
            }
        }
    }

    companion object {
        private const val TAG = "CacheManager"
    }
}
