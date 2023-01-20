package com.example.mapwidgetdemo.ui.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.ConnectionLiveData
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.example.mapwidgetdemo.utils.isConnectedToInternet
import com.example.mapwidgetdemo.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseActivity: AppCompatActivity(){

    private var connectionLiveData: ConnectionLiveData? = null
    val loginViewModel: LoginViewModel by viewModel()

    open var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationManager: LocationManager? = null
    private val minTime: Long = 10000
    private val minDistance = 10f

    private var isGPSEnabled = false

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        loginViewModel.isNetworkAvailable.value = isConnectedToInternet()
        connectionLiveData = ConnectionLiveData(this)

    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!

        when {
            !isGPSEnabled -> { //
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            else -> {
                getLocation()
            }
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = true
        criteria.isCostAllowed = true
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH

        val providers = locationManager!!.getProviders(criteria, true)
        for (provider in providers) {
            if (!provider.contains("gps")) { // if gps is disabled
                val poke = Intent()
                poke.setClassName(
                    "com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"
                )
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
                poke.data = Uri.parse("3")
                this.sendBroadcast(poke)
            } // Get the location from the given provider
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            } else {
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener
                )
            }
        }
    }

    private val locationListener = LocationListener {
        currentLatitude = it.latitude
        currentLongitude = it.longitude
        SharedPreferenceUtils.preferencePutString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE, currentLatitude.toString())
        SharedPreferenceUtils.preferencePutString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE, currentLongitude.toString())
//        Toast.makeText(this, "Cur Lat/Long$currentLatitude,$currentLongitude", Toast.LENGTH_SHORT).show();
    }

    fun checkPermission(): Boolean {
        val location = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val write = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val read = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val camera = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.CAMERA
        )
        val audio = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        return location == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
                && camera == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }
}
