package com.samz.weatherapp.remote

class MainRepository(private val apiInterface: APIInterface) {

    suspend fun getCityWeather(cityName: String) =
        apiInterface.getCityWeather(cityName, "metric",ApiClient.weatherAPIKey)
}