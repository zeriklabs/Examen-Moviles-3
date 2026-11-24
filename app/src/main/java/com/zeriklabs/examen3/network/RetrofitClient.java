package com.zeriklabs.examen3.network;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://dummyjson.com/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS);

            if (context != null) {
                try {
                    File cacheDir = new File(context.getCacheDir(), "http_cache");
                    Cache cache = new Cache(cacheDir, 10 * 1024 * 1024); // 10 MB Cache
                    okHttpClientBuilder.cache(cache);
                } catch (Exception ignored) {
                }
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static ApiService getApiService() {
        return getApiService(null);
    }
}
