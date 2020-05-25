package com.udacity.astroapp.utils;

import com.udacity.astroapp.models.MarsPhotoObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MarsPhotoService {

    @GET("photos?")
    Call<MarsPhotoObject> getMarsPhotoObject(@Query("earth_date") String date, @Query("api_key") String apiKey,
                                             @Query("page") String page);

}