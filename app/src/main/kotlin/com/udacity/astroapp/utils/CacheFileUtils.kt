package com.udacity.astroapp.utils

import android.content.Context
import android.util.Log
import java.io.File

object CacheFileUtils {
    private const val TAG = "CacheFileUtils"

    /** Calculate the total size of a directory in bytes */
    fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        try {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to calculate directory size for ${directory.path}", e)
        }
        return size
    }

    /** Clear oldest cache entries until the target size is reached */
    fun clearOldestCacheEntries(cacheDir: File, targetSize: Long): Boolean {
        return try {
            val files =
                cacheDir.walkTopDown().filter { it.isFile }.sortedBy { it.lastModified() }.toList()

            var currentSize = calculateDirectorySize(cacheDir)
            var index = 0

            while (currentSize > targetSize && index < files.size) {
                val file = files[index]
                val fileSize = file.length()
                if (file.delete()) {
                    currentSize -= fileSize
                    Log.d(TAG, "Deleted cache file: ${file.name}, size: $fileSize")
                }
                index++
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear oldest cache entries", e)
            false
        }
    }

    /** Get or create a cache directory */
    fun getCacheDirectory(context: Context, subdirectory: String): File {
        val cacheDir = File(context.cacheDir, subdirectory)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /** Delete a cache directory and all its contents */
    fun deleteCacheDirectory(context: Context, subdirectory: String): Boolean {
        return try {
            val cacheDir = File(context.cacheDir, subdirectory)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                Log.d(TAG, "$subdirectory cache cleared")
                true
            } else {
                true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear $subdirectory cache", e)
            false
        }
    }

    /** Get cache directory names for all cache types */
    object CacheDirectories {
        const val HTTP_CACHE = "http_cache"
        const val IMAGE_CACHE = "image_cache"
    }
}
