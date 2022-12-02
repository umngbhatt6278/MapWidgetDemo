package com.example.mapwidgetdemo.ui.activity

import android.Manifest
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    lateinit var videoUri: Uri
    private lateinit var locationRequest: LocationRequest

    private lateinit var map: GoogleMap
    private val TAG = "mytag"
    private val REQUEST_LOCATION_PERMISSION = 1
    private var locationManager: LocationManager? = null
    var zoomWidth: Int? = null
    var zoomHeight: Int? = null
    var zoomPadding: Double? = null
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    private var isVideoStarted = false

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }

    private val contract = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        if (it) {
            fetchCurrentLocation()
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val manager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        saveVideoWithLocation(location.latitude, location.longitude)
                    }
                    .addOnFailureListener {
                        Log.e("TAG", "fetchLocation() --> Fail")
                    }
            }
        }
    }

    private fun saveVideoWithLocation(latitude: Double, longitude: Double) {
        wordViewModel.insert(MarkerModel(latitude = latitude,
            longitude = longitude,
            videopath = videoUri.toString()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()){
                    for (location in locationResult.locations) {
                        saveVideoWithLocation(location.latitude, location.longitude)
                    }
                }
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

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

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun createFileUri(): Uri? {
        val sdf = SimpleDateFormat("dd_M_yyyy_hh_mm_ss", Locale.ENGLISH)
        val currentDate = sdf.format(Date())
        val video = File(applicationContext.filesDir, "$currentDate.mp4")
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.mapwidgetdemo.fileProvider",
            video
        )
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
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

                if (!data.isNullOrEmpty()){
                    for (i in data?.indices!!) {
                        builder.include(LatLng(data?.get(i)?.latitude!!, data?.get(i)?.longitude!!))
                        createMarker(
                            LatLng(data[i].latitude, data[i].longitude),
                            "Title One ==> $i",
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


        map.setOnMarkerClickListener(this)

    }

    protected open fun createMarker(
        latlang: LatLng,
        title: String?,
        snippet: String?
    ): Marker? {
        return map.addMarker(
            MarkerOptions().position(latlang).anchor(0.5f, 0.5f).title(title).snippet(snippet)
        )
    }

    open fun checkPermission(): Boolean {
        val location = ContextCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)
        val write = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val read = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        val camera = ContextCompat.checkSelfPermission(applicationContext, CAMERA)
        val audio = ContextCompat.checkSelfPermission(applicationContext, RECORD_AUDIO)
        return location == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
                && camera == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED
    }

    open fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO),
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
//                    onMapReady(map)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(
                                                ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE,
                                                READ_EXTERNAL_STORAGE, CAMERA, RECORD_AUDIO
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
        Log.d("mytag","onLocationChanged ==> " + latLng.toString())
        locationManager?.removeUpdates(this);
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        Toast.makeText(this, marker?.title, Toast.LENGTH_SHORT).show();
        val intent = Intent(this, VideoActivity::class.java)
        startActivity(intent)
        return true
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG,"onCreate() --> ${intent.getBooleanExtra("IS_FROM_WIDGET", false)}")

        if (!isVideoStarted && intent.getBooleanExtra("IS_FROM_WIDGET", false)){
            videoUri = createFileUri()!!
            if (checkCameraHardware(this)) {
                contract.launch(videoUri)
            }
            isVideoStarted = true
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}