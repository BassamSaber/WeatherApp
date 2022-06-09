package com.samz.weatherapp.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val baseURL = "https://api.openweathermap.org/data/"

    const val weatherAPIKey = "0afd39f8b0e250ce7b4af6dfefbb893a"

    fun <T> getAPIClient(api: Class<T>): T {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()

//        if (BuildConfig.DEBUG) {
        httpClient.addInterceptor(logging)
//        }
        httpClient.connectTimeout(60, TimeUnit.SECONDS)
        httpClient.readTimeout(60, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build().create(api)
    }

}