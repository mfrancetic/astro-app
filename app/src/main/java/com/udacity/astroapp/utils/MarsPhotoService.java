package com.udacity.astroapp.utils;

import com.udacity.astroapp.models.MarsPhoto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MarsPhotoService {

    @GET("photos?")
    Call<List<MarsPhoto>> marsPhotoList(@Query("earth_date") String date, @Query("api_key") String apiKey,
                                        @Query("page") String page);

}