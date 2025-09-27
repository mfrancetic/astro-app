package com.udacity.astroapp.di

import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.udacity.astroapp.utils.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageModule = module {
    single {
        val context = androidContext()

        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(Constants.IMAGE_MEMORY_CACHE_SIZE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(Constants.IMAGE_DISK_CACHE_SIZE)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false) // Use our own cache strategy
            .logger(DebugLogger())
            .build()
    }
}
