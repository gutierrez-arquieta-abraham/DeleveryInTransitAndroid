package com.PolicodeLabs.Delevery_In_Transit.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ¡OJO! Si usas el EMULADOR de Android, tu PC no es "localhost", es "10.0.2.2".
    // ¡OJO! Si usas el Android, es "http://192.168.137.1:8080/".
    private static final String BASE_URL = "http://192.168.137.1:8080/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}