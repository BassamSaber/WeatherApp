package com.samz.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.samz.weatherapp.R
import com.samz.weatherapp.databinding.ActivityMainBinding
import com.samz.weatherapp.remote.APIInterface
import com.samz.weatherapp.remote.ApiClient
import com.samz.weatherapp.remote.MainRepository
import com.samz.weatherapp.utils.GpsUtils
import com.samz.weatherapp.utils.MyViewModelFactory
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mainRepository = MainRepository(ApiClient.getAPIClient(APIInterface::class.java))
        viewModel =
            ViewModelProvider(
                this,
                MyViewModelFactory(mainRepository)
            ).get(MainViewModel::class.java)

        binding.viewModel = viewModel
        binding.executePendingBindings()

        binding.etSearch.setOnEditorActionListener(object :TextView.OnEditorActionListener{
            override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event?.action == KeyEvent.ACTION_DOWN
                    && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    viewModel.onGetSearchCityWeather()
                    return true;
                }
                return false
            }
        })
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onStart() {
        super.onStart()

        if(isLocationEnabled()) {
            checkPermissions {
                getCurrentLocationWeather()
            }
        }else{
            GpsUtils(this).turnGPSOn {
                isGPSEnable->
                if(isGPSEnable)
                checkPermissions {
                    getCurrentLocationWeather()
                }
            }
        }
    }

    private fun isLocationEnabled():Boolean{

        val locationMode: Int = try {
            Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==121){
            checkPermissions{
                getCurrentLocationWeather()
            }
        }else
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.UK)
                val result =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!result.isNullOrEmpty())
                    viewModel.getCityWeather(result[0].adminArea)

            }
        }
            .addOnFailureListener { _ ->
            }
    }

    private fun checkPermissions(callback: (() -> Unit)? = null) {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    requestLocation()
                    callback?.let {
                        callback()
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.please_enable_location),
                        Toast.LENGTH_SHORT
                    ).show()
                    callback?.let {
                        callback()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.run {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val mLocationCallback: LocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        if (location != null) {
                            fusedLocationClient.requestLocationUpdates(null, null)
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }
    }
}