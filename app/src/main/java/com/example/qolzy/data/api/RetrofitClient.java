package com.example.qolzy.data.api;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final int TIMEOUT = 30; // seconds
    private static final Map<String, Retrofit> retrofitMap = new HashMap<>();

    public static Retrofit getInstance(String baseUrl, Context context) {
        if (!retrofitMap.containsKey(baseUrl)) {
            synchronized (RetrofitClient.class) {
                if (!retrofitMap.containsKey(baseUrl)) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(context))
                            .addInterceptor(logging)
                            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .build();

                    retrofitMap.put(baseUrl, retrofit);
                }
            }
        }
        return retrofitMap.get(baseUrl);
    }

    public static void clearInstance(String baseUrl) {
        retrofitMap.remove(baseUrl);
    }

}