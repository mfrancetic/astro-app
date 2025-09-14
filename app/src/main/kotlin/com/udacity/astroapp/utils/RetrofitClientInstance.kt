package com.udacity.astroapp.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientInstance {

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var client: OkHttpClient.Builder? = null

    fun getRetrofitInstance(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(Constants.MARS_PHOTO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getClient().build())
                .build()
                .also { retrofit = it }
        }
    }

    private fun getClient(): OkHttpClient.Builder {
        return client ?: synchronized(this) {
            client ?: OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .also { client = it }
        }
    }
}