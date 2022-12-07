package com.example.mapwidgetdemo.ui.activity.location

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * MutableLiveData private field to get/save location updated values
     */
    private val locationData =
        LocationLiveData(application)

    /**
     * LiveData a public field to observe the changes of location
     */
    val getLocationData: LiveData<Location> = locationData
}