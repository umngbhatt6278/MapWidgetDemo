package com.example.mapwidgetdemo.ui.activity

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.mapwidgetdemo.databinding.ActivityCameraBinding
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.ui.activity.location.LocationUtil
import com.example.mapwidgetdemo.ui.activity.location.LocationViewModel
import java.text.SimpleDateFormat
import java.util.*


class CameraActivity : BaseActivity() {
    private lateinit var mBinding: ActivityCameraBinding
    private var recording: Recording? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationManager: LocationManager? = null
    private val minTime: Long = 10000
    private val minDistance = 10f


    private lateinit var locationViewModel: LocationViewModel
    private var isGPSEnabled = false


    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.ivStop.setOnClickListener {
            recording?.stop()
            finish()
        }

        locationViewModel = LocationViewModel(application)
        LocationUtil(this).turnGPSOn(object : LocationUtil.OnLocationOnListener {
            override fun locationStatus(isLocationOn: Boolean) {
                this@CameraActivity.isGPSEnabled = isLocationOn
            }
        })

        //        getLocation()

        startCamera()
    }

    private fun observeLocationUpdates() {
        locationViewModel.getLocationData.observe(this) {
            Log.d("location", "observeLocationUpdates lat/lon ==> ${it.latitude} ${it.longitude}")
            currentLatitude = it.latitude
            currentLongitude = it.longitude
            mBinding.txtLatLong.text = "${"Current Lat/Long"} : $currentLatitude,$currentLongitude"
        }
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }


    private fun startLocationUpdates() {
        when {
            !isGPSEnabled -> { //
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

            checkPermission() -> {
                observeLocationUpdates()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({ // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(mBinding.surfaceView.surfaceProvider)
            }

            val recorder =
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            val videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try { // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

                captureVideo(videoCapture)
            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureVideo(videoCapture: VideoCapture<Recorder>) {

        val curRecording = recording
        if (curRecording != null) { // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name =
            SimpleDateFormat("dd_mm_yyyy_hh_mm_ss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions =
            MediaStoreOutputOptions.Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setContentValues(contentValues).build()
        recording = videoCapture.output.prepareRecording(this, mediaStoreOutputOptions).apply {
            if (PermissionChecker.checkSelfPermission(
                    this@CameraActivity, Manifest.permission.RECORD_AUDIO
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                withAudioEnabled()
            }
        }.start(ContextCompat.getMainExecutor(this)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    Toast.makeText(baseContext, "Recording started", Toast.LENGTH_SHORT).show()
                }
                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        val msg = "Video Saved to" + "${recordEvent.outputResults.outputUri}"
                        Log.e("TAG", msg)
                        saveVideoWithLocation(currentLatitude, currentLongitude, recordEvent.outputResults.outputUri)
                    } else {
                        recording?.close()
                        recording = null
                        Log.e(
                            "TAG", "Video capture ends with error: " + "${recordEvent.cause?.localizedMessage}"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun saveVideoWithLocation(latitude: Double, longitude: Double, videoUri: Uri) {
        wordViewModel.insert(
            MarkerModel(
                latitude = latitude, longitude = longitude, videopath = videoUri.toString()
            )
        )
        Toast.makeText(this, "Recording Saved", Toast.LENGTH_SHORT).show()
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager!!.allProviders
        for (provider in providers) {
            if (!provider.contains("gps")) { // if gps is disabled
                val poke = Intent()
                poke.setClassName(
                    "com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"
                )
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
                poke.data = Uri.parse("3")
                sendBroadcast(poke)
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

        //        Toast.makeText(this, "updated lat/lon ==> $currentLatitude $currentLongitude", Toast.LENGTH_SHORT).show();

        //        startCamera()
    }

    override fun onPause() {
        super.onPause()
        locationManager?.removeUpdates(locationListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty()) { //            getLocation()
            isGPSEnabled = true
            startLocationUpdates()
        }
    }
}