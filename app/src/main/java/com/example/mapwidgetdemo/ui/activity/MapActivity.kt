package com.example.mapwidgetdemo.ui.activity

import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivityMapBinding
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar


open class MapActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener {

    private lateinit var map: GoogleMap
    private var zoomWidth: Int? = null
    private var zoomHeight: Int? = null
    private var zoomPadding: Double? = null

    private lateinit var binding: ActivityMapBinding

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (!checkPermission()) {
            requestPermission()
        } else {
            mapFragment.getMapAsync(this)
            zoomWidth = resources.displayMetrics.widthPixels
            zoomHeight = resources.displayMetrics.heightPixels
            zoomPadding = (zoomWidth!! * 0.10) // offset

        }

        binding.btnpins.setOnClickListener {
            val intent = Intent(this, MapPinActivity::class.java)
            startActivity(intent)
        }

    }




    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = true
        if (ActivityCompat.checkSelfPermission(
                this, ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } //        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map.isMyLocationEnabled = true
        map.isTrafficEnabled = false
        map.isBuildingsEnabled = true
        map.isIndoorEnabled = true

        map.uiSettings.isCompassEnabled = true

        map.uiSettings.setAllGesturesEnabled(true)
        LatLngBounds.Builder()

        wordViewModel.allWords.observe(this@MapActivity) { words -> // Update the cached copy of the words in the adapter.
            words.let {
                val data = it
                if (!data.isNullOrEmpty()) {
                    for (i in data.indices) {
                        createMarker(
                            LatLng(data[i].latitude, data[i].longitude), data[i].videopath, LatLng(data[i].latitude, data[i].longitude).toString()
                        )
                    }
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                data[0].latitude, data[0].longitude
                            ), 17f
                        )
                    )

                }
            }
        }
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
    }

    protected open fun createMarker(latlang: LatLng, title: String?, snippet: String?): Marker? {
        return map.addMarker(
            MarkerOptions().position(latlang).title(title)
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> if (grantResults.isNotEmpty()) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val readAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED

                if (locationAccepted && writeAccepted && readAccepted) { //                    onMapReady(map)
                } else {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel(
                            "You need to allow access to both the permissions"
                        ) { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO
                                ), REQUEST_LOCATION_PERMISSION
                            )
                        }
                        return
                    }
                }
            }
        }
    }

    open fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MapActivity).setMessage(message).setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val intent = Intent(this, VideoActivity::class.java).putExtra("VideoPath", marker?.title)
        startActivity(intent)
        return true
    }

    override fun onMapLongClick(marker: LatLng?) { //        Toast.makeText(this, "${marker?.latitude} ${marker?.longitude}", Toast.LENGTH_SHORT).show()
        val snackbar = Snackbar.make(
            binding.mainlayout, "Latitude:${marker?.latitude} Longitude:${marker?.longitude}", Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }
}