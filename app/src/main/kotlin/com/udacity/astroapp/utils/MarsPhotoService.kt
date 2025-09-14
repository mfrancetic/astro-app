package com.udacity.astroapp.utils

import com.udacity.astroapp.models.MarsPhotoObject
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MarsPhotoService {

    @GET("photos?")
    fun getMarsPhotoObject(
        @Query("earth_date") date: String,
        @Query("api_key") apiKey: String,
        @Query("page") page: String
    ): Call<MarsPhotoObject>
}