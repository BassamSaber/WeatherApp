package com.samz.weatherapp.ui

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.samz.weatherapp.remote.MainRepository
import kotlinx.coroutines.*

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    val isLoading = ObservableBoolean(false)

    val errorMsg = ObservableField("")
    val cityName = ObservableField("")
    val weatherTemp = ObservableField("")
    val weatherValue = ObservableField("")

    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    fun onGetSearchCityWeather() {
        if (cityName.get()?.isNotEmpty() == true)
            getCityWeather(cityName.get()!!)
    }

    fun getCityWeather(cityName: String) {
        if (this.cityName.get() != cityName)
            this.cityName.set(cityName)
        if(errorMsg.get()?.isNotEmpty()==true){
            errorMsg.set("")
            weatherTemp.set("")
            weatherValue.set("")
        }
        isLoading.set(true)
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getCityWeather(cityName)
            withContext(Dispatchers.Main) {
                isLoading.set(false)
                if (response.isSuccessful) {
                    weatherValue.set(response.body()?.weather?.let { if(it.isNotEmpty()) it[0].main else "" }?:"")
                    weatherTemp.set(response.body()?.main?.temp?.toInt()?.toString()?:"")
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }

    }

    private fun onError(errorMsg: String) {
        this.errorMsg.set(errorMsg)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}