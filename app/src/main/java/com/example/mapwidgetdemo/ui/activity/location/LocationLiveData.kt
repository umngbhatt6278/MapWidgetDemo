package com.example.mapwidgetdemo.ui.activity.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener


/**
 * Constants Values
 */
const val INTERVAL = 1000L
const val FASTEST_INTERVAL = 500L

class LocationLiveData(context: Context) : MutableLiveData<Location>() {


    private var fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private fun setLocationData(location: Location) {
        value = location
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
                interval = INTERVAL
                fastestInterval = FASTEST_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()/*  fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }*/


        fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, object :
            CancellationToken() {
            override fun isCancellationRequested(): Boolean {
                return false
            }

            override fun onCanceledRequested(onTokenCanceledListener: OnTokenCanceledListener): CancellationToken {
                return this
            }
        }).addOnSuccessListener(OnSuccessListener { location: Location ->
            location?.also {
                setLocationData(it)
            }
        })


        startLocationUpdates()
    }

    /**
     * Callback that triggers on location updates available
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    /**
     * Initiate Location Updates using Fused Location Provider and
     * attaching callback to listen location updates
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, null
        )
    }

}