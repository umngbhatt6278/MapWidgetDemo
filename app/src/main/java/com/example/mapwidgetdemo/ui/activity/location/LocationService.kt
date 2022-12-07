package com.example.mapwidgetdemo.ui.activity.location

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder

import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager





class LocationService : Service() {
    private var mLocationManager: LocationManager? = null

    private val mBinder: IBinder = LocalBinder()



    var mLocationListeners = arrayOf(
        LocationListener(LocationManager.GPS_PROVIDER),
        LocationListener(LocationManager.NETWORK_PROVIDER)
    )

    inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)

            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, location)
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent)

            Log.v(
                "LastLocation",
                mLastLocation.latitude.toString() + "  " + mLastLocation.longitude.toString()
            )
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        initializeLocationManager()
        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[1]
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[0]
            )
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex)
                }

            }
        }
    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    companion object {
        private val TAG = "BOOMBOOMTESTGPS"
        private val LOCATION_INTERVAL = 1000
        private val LOCATION_DISTANCE = 0f

        private val PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice"

        val ACTION_BROADCAST: String = PACKAGE_NAME + ".broadcast"

        val EXTRA_LOCATION: String = PACKAGE_NAME + ".location"
    }
}