package com.sharpsec.fmw.auth.siam;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


class APIClient {

    private static Retrofit retrofit = null;
    private static String siamAuthUrl = "https://172.29.214.109:3400";

    static Retrofit getClient() {

        //Siam uses https protocol (self signed) - just bypassing for now
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();

        retrofit = new Retrofit.Builder()
                .baseUrl(siamAuthUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit;
    }
}
