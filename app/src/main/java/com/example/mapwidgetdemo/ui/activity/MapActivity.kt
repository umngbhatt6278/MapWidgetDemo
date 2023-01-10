package com.example.mapwidgetdemo.ui.activity

import android.Manifest.permission.*
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivityMapBinding
import com.example.mapwidgetdemo.services.ForegroundService
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.google.android.exoplayer2.offline.DownloadService.startForeground
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.File


open class MapActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener {

    private lateinit var map: GoogleMap
    private var zoomWidth: Int? = null
    private var zoomHeight: Int? = null
    private var zoomPadding: Double? = null
    var uploaddatalistforserver: ArrayList<MarkerModel> = ArrayList()
    private lateinit var binding: ActivityMapBinding

    var mcount = 0

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


        binding.btnsync.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
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

        map.uiSettings.setAllGesturesEnabled(true) //        LatLngBounds.Builder()

        val builder = LatLngBounds.Builder()

        wordViewModel.allWords.observe(this@MapActivity) { words -> // Update the cached copy of the words in the adapter.
            words.let {
                val data = it
                uploaddatalistforserver = data as ArrayList<MarkerModel>
                if (!data.isNullOrEmpty()) {
                    for (i in data.indices) {
                        builder.include(LatLng(data[i].latitude, data[i].longitude))
                        createMarker(
                            LatLng(data[i].latitude, data[i].longitude), data[i].videopath, LatLng(data[i].latitude, data[i].longitude).toString()
                        )
                    }

                    val bounds = builder.build()

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            bounds, zoomWidth!!, zoomHeight!!, zoomPadding!!.toInt()
                        )
                    )
                }

                if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER)) {
                    if (SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)) {
                        startService()
                    }
                }
            }
        }
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d("logger", "Token ==> " + SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.F_TOKEN).toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    open fun startService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android") //        ContextCompat.startForegroundService(this, serviceIntent)
        bindService(serviceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE)

    }

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.LocalBinder

            Log.d("service", "Service connected")
            createNotificationChannel()
            val notificationIntent = Intent(this@MapActivity, MapActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this@MapActivity, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val notification: Notification =
                NotificationCompat.Builder(this@MapActivity, ForegroundService.CHANNEL_ID).setContentTitle("Foreground Service").setContentText("Sevice Starteed").setSmallIcon(R.drawable.ic_pin).setContentIntent(pendingIntent).build()
            binder.service.startForeground(1, notification)

            fireAndForgetNetworkCall()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    fun fireAndForgetNetworkCall() {
        Log.i("logger", "-----Async network calls without error handling-----")


        lifecycleScope.executeAsyncTask(onPreExecute = { // ...
        }, doInBackground = { // ...
            "Result" // send data to "onPostExecute"
            Log.d("logger", "Video Uploading Sucessfully on server")

            if (!uploaddatalistforserver[mcount].isserver) {
                loginViewModel.saveVideo(
                    uploaddatalistforserver[mcount].latitude, uploaddatalistforserver[mcount].longitude, uploaddatalistforserver[mcount].videoname, uploaddatalistforserver[mcount].videopath
                )
            }else{
                mcount += 1
            }
        }, onPostExecute = { // ... here "it" is a data returned from "doInBackground"
            lifecycleScope.launch {
                loginViewModel.allEventsFlow.collect { event ->
                    when (event) {
                        is AllEvents.SuccessBool -> {
                            when (event.code) {
                                1 -> {
                                    Log.d("mytag", "Video Uploaded Sucessfully")
                                    if (mcount < uploaddatalistforserver.size) {
                                        val memo = MarkerModel(
                                            id = uploaddatalistforserver[mcount].id, latitude = uploaddatalistforserver[mcount].latitude, longitude = uploaddatalistforserver[mcount].longitude, videopath = uploaddatalistforserver[mcount].videopath, videoname = uploaddatalistforserver[mcount].videoname, isserver = true
                                        )
                                        wordViewModel.update(memo)
                                        mcount += 1
                                        fireAndForgetNetworkCall()
                                    } else {
                                        Log.d("mytag", "all Video Uploaded Sucessfully")
                                        mcount = 0
                                    }
                                }
                            }
                        }
                        else -> {
                            val asString = event.asString(this@MapActivity)
                            if (asString !is Unit && asString.toString().isNotBlank()) {
                                Toast.makeText(
                                    this@MapActivity, asString.toString(), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        })

    }

    fun <R> CoroutineScope.executeAsyncTask(onPreExecute: () -> Unit, doInBackground: () -> R, onPostExecute: (R) -> Unit) =
        launch {
            onPreExecute()
            val result =
                withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
                    doInBackground()
                }
            onPostExecute(result)
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                ForegroundService.CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    open fun stopService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        stopService(serviceIntent)
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