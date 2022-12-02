package com.example.mapwidgetdemo.ui.activity

import android.Manifest
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


open class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var map: GoogleMap
    private val TAG = "mytag"
    private val REQUEST_LOCATION_PERMISSION = 1
    private var locationManager: LocationManager? = null
    var zoomWidth: Int? = null
    var zoomHeight: Int? = null
    var zoomPadding: Double? = null
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment


        var word = MarkerModel(
            latitude = 23.012043440161523, longitude = 72.50499295485864,
            videopath = ""
        )
        wordViewModel.insert(word)

        word = MarkerModel(
            latitude = 23.028979364949823, longitude = 72.49916689205622,
            videopath = ""
        )
        wordViewModel.insert(word)

        word = MarkerModel(
            latitude = 23.034943022650506, longitude = 72.49775068574698,
            videopath = ""
        )
        wordViewModel.insert(word)

        word = MarkerModel(
            latitude = 23.02854491599756, longitude = 72.51247064829457,
            videopath = ""
        )
        wordViewModel.insert(word)



        if (!checkPermission()) {
            requestPermission()
        } else {
            try {
                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                if (!locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                } else {
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME,
                        MIN_DISTANCE,
                        this
                    )
                    mapFragment.getMapAsync(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            zoomWidth = resources.displayMetrics.widthPixels
            zoomHeight = resources.displayMetrics.heightPixels;
            zoomPadding = (zoomWidth!! * 0.10); // offset

        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        map.isMyLocationEnabled = true

        val builder = LatLngBounds.Builder()

        wordViewModel.allWords.observe(this@MapActivity) { words ->
            // Update the cached copy of the words in the adapter.
            words.let {
                val data = it

                for (i in data?.indices!!) {
                    builder.include(LatLng(data?.get(i)?.latitude!!, data?.get(i)?.longitude!!))
                    createMarker(
                        LatLng(data[i].latitude, data[i].longitude),
                        "Title One",
                        "Snippet1$i"
                    )
                }

                val bounds = builder.build();
                val handler = Handler()
                handler.postDelayed(Runnable {
                    map.animateCamera(
                        zoomWidth?.let { width: Int ->
                            zoomHeight?.let { height ->
                                zoomPadding?.let { padding ->
                                    CameraUpdateFactory.newLatLngBounds(
                                        bounds,
                                        width,
                                        height,
                                        padding.toInt()
                                    )
                                }
                            }
                        }
                    )
                }, 500)
            }
        }


    }

    protected open fun createMarker(
        latlang: LatLng,
        title: String?,
        snippet: String?
    ): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(latlang)
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
        )
    }

    open fun checkPermission(): Boolean {
        val location = ContextCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)
        val write = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return location == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
    }

    open fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            REQUEST_LOCATION_PERMISSION
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> if (grantResults.size > 0) {
                val locationAccepted = grantResults[0] === PackageManager.PERMISSION_GRANTED
                val writeAccepted = grantResults[1] === PackageManager.PERMISSION_GRANTED
                val readAccepted = grantResults[2] === PackageManager.PERMISSION_GRANTED
                if (locationAccepted && writeAccepted && readAccepted) {
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(
                                                ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE,
                                                READ_EXTERNAL_STORAGE
                                            ),
                                            REQUEST_LOCATION_PERMISSION
                                        )
                                    }
                                })
                            return
                        }
                    }
                }
            }
        }
    }

    open fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this@MapActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
//        map.animateCamera(cameraUpdate)
        locationManager?.removeUpdates(this);
    }
}