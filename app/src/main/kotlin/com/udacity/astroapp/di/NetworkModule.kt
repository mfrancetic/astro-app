package com.udacity.astroapp.di

import android.content.Context
import com.udacity.astroapp.data.api.MarsPhotoService
import com.udacity.astroapp.utils.Constants
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY } }

    single {
        val cacheSize = Constants.HTTP_CACHE_SIZE
        val cacheDir = File(androidContext().cacheDir, "http_cache")
        Cache(cacheDir, cacheSize)
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                val cacheControl =
                    CacheControl.Builder()
                        .maxAge(Constants.HTTP_CACHE_MAX_AGE_HOURS, TimeUnit.HOURS)
                        .build()

                originalResponse
                    .newBuilder()
                    .header("Cache-Control", cacheControl.toString())
                    .build()
            }
            .addInterceptor { chain ->
                var request = chain.request()

                // Force cache usage when offline
                if (!isNetworkAvailable(androidContext())) {
                    val cacheControl =
                        CacheControl.Builder()
                            .maxStale(Constants.HTTP_CACHE_MAX_STALE_DAYS, TimeUnit.DAYS)
                            .onlyIfCached()
                            .build()

                    request = request.newBuilder().cacheControl(cacheControl).build()
                }

                chain.proceed(request)
            }
            .cache(get())
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(Constants.MARS_PHOTO_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<MarsPhotoService> { get<Retrofit>().create(MarsPhotoService::class.java) }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return networkCapabilities.hasCapability(
        android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
    )
}
