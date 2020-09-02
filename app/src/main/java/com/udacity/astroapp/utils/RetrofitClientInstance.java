package com.udacity.astroapp.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static OkHttpClient.Builder client;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.MARS_PHOTO_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getClient().build())
                    .build();
        }
        return retrofit;
    }

    private static OkHttpClient.Builder getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder();
        }
        client.connectTimeout(20, TimeUnit.SECONDS);
        client.readTimeout(20, TimeUnit.SECONDS);
        return client;
    }
}