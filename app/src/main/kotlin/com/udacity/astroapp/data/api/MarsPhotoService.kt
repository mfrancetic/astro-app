package com.udacity.astroapp.data.api

import com.udacity.astroapp.models.MarsPhotoObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MarsPhotoService {

    @GET("photos")
    suspend fun getMarsPhotos(
        @Query("earth_date") date: String,
        @Query("api_key") apiKey: String,
        @Query("page") page: String = "1"
    ): Response<MarsPhotoObject>

    @GET("photos")
    suspend fun getMarsPhotosBySol(
        @Query("sol") sol: String,
        @Query("api_key") apiKey: String,
        @Query("page") page: String = "1"
    ): Response<MarsPhotoObject>
}