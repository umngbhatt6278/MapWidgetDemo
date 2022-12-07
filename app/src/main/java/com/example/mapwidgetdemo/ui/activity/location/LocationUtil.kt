package com.example.mapwidgetdemo.ui.activity.location

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.example.mapwidgetdemo.ui.activity.REQUEST_LOCATION_PERMISSION
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient

class LocationUtil(private val context: Context) {

    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
    private val locationSettingsRequest: LocationSettingsRequest?
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationLiveData.locationRequest)
        locationSettingsRequest = builder.build()
        builder.setAlwaysShow(true)
    }

    /**
     *
     */
    fun turnGPSOn(OnGpsListener: OnLocationOnListener?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGpsListener?.locationStatus(true)
        } else {
            locationSettingsRequest?.let {
                settingsClient.checkLocationSettings(it).addOnSuccessListener(context as Activity) { // GPS enabled already
                        OnGpsListener?.locationStatus(true)
                    }.addOnFailureListener(context) { e ->
                        when ((e as ApiException).statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->

                                try {
                                    val rae = e as ResolvableApiException
                                    rae.startResolutionForResult(context, REQUEST_LOCATION_PERMISSION)
                                } catch (sie: IntentSender.SendIntentException) {
                                    Log.i("TAG", "PendingIntent unable to execute request.")
                                }

                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                val errorMessage = "Enable location services from settings."
                                Log.e("TAG", errorMessage)

                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
        }
    }

    /**
     *
     */
    interface OnLocationOnListener {
        fun locationStatus(isLocationOn: Boolean)
    }
}