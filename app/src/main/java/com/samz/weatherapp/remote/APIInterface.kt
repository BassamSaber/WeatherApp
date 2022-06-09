package com.samz.weatherapp.remote

import com.samz.weatherapp.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {
    @GET("2.5/weather")
    suspend fun getCityWeather(
        @Query("q") cityName: String,
        @Query("units") units: String,
        @Query("APPID") ApiKey: String
    ): Response<WeatherResponse>
}