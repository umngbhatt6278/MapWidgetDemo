package com.example.mapwidgetdemo.ui.activity.fragment

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivityMapBinding
import com.example.mapwidgetdemo.request.EditMarkerRequestModel
import com.example.mapwidgetdemo.response.GetVideoResponse
import com.example.mapwidgetdemo.services.ForegroundService
import com.example.mapwidgetdemo.ui.activity.*
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapViewFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private lateinit var map: GoogleMap
    private var zoomWidth: Int? = null
    private var zoomHeight: Int? = null
    private var zoomPadding: Double? = null
    var uploaddatalistforserver: ArrayList<MarkerModel> = ArrayList()
    var mcount = 0


    var markerList: ArrayList<Marker> = ArrayList()
    var latList: ArrayList<LatLng> = ArrayList()

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((activity?.application as MainApplication).repository)
    }


    var mapvideodatalist: ArrayList<MarkerModel> = ArrayList()
    val builder = LatLngBounds.Builder()

    lateinit var fragmentmapBinding: ActivityMapBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentmapBinding = ActivityMapBinding.inflate(inflater, container, false)
        return fragmentmapBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment //        val mapFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        if (!checkPermission()) {
            requestPermission()
        } else {
            mapFragment.getMapAsync(this)
        }


        fragmentmapBinding.btnsync.setOnClickListener {


            getCornerBoundsPointList()

             val intent = Intent(activity!!, SettingsActivity::class.java)
             startActivity(intent)
        }


        wordViewModel.allWords.observe(this@MapViewFragment) { words -> // Update the cached copy of the words in the adapter.
            uploaddatalistforserver = words as ArrayList<MarkerModel>
        }
    }


    fun getCornerBoundsPointList(){
        if (map != null) {
            var pincount = 0
            val tempListdata: ArrayList<MarkerModel> = ArrayList()
            for (i in mapvideodatalist.indices) {
                if (map.projection.visibleRegion.latLngBounds.contains(LatLng(mapvideodatalist[i].latitude, mapvideodatalist[i].longitude))) {
                    pincount = pincount + 1
                    tempListdata.add(
                        MarkerModel(
                            mapvideodatalist[i].id, mapvideodatalist[i].latitude, mapvideodatalist[i].longitude, mapvideodatalist[i].videopath, mapvideodatalist[i].videoname, mapvideodatalist[i].isserver
                        )
                    )
                }
            }
            Toast.makeText(requireActivity(), "Bounds Pin Counnt ==> " + pincount.toString(), Toast.LENGTH_SHORT).show()
            SharedPreferenceUtils.saveArrayList(tempListdata, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)
        }
    }


    private fun getApiData() {
        loginViewModel.getVideosFromApi()

        lifecycleScope.launch {
            loginViewModel.allEventsFlow.collect { event ->
                when (event) {
                    is AllEvents.SuccessBool -> {
                        when (event.code) {
                            1 -> {
                            }
                        }
                    }
                    is AllEvents.Success<*> -> {
                        val videolist = event.data as GetVideoResponse
                        mapvideodatalist = ArrayList()

                        if (map != null) {
                            map.clear()
                            markerList.clear()
                        }

                        val builder = LatLngBounds.Builder()

                        if (videolist.data.videos.isNotEmpty()) {
                            for (i in videolist.data.videos.indices) {
                                mapvideodatalist.add(
                                    MarkerModel(
                                        videolist.data.videos[i].id, videolist.data.videos[i].lat, videolist.data.videos[i].long, videolist.data.videos[i].video, videolist.data.videos[i].name, true
                                    )
                                )
                            }
                            for (j in uploaddatalistforserver.indices) {
                                if (!uploaddatalistforserver[j].isserver) {
                                    mapvideodatalist.add(
                                        MarkerModel(
                                            uploaddatalistforserver[j].id, uploaddatalistforserver[j].latitude, uploaddatalistforserver[j].longitude, uploaddatalistforserver[j].videopath, uploaddatalistforserver[j].videoname, uploaddatalistforserver[j].isserver
                                        )
                                    )

                                }
                            }
                            for (i in mapvideodatalist.indices) {
//                                builder.include(LatLng(mapvideodatalist[i].latitude, mapvideodatalist[i].longitude))
                                var marker: Marker
                                var userIndicator = MarkerOptions()
                                if (mapvideodatalist[i].isserver) {
                                    userIndicator =
                                        MarkerOptions().position(LatLng(mapvideodatalist[i].latitude, mapvideodatalist[i].longitude))
                                            .draggable(true)
                                            .title(mapvideodatalist[i].videoname)
                                            .snippet("lat:" + mapvideodatalist[i].latitude.toString() + ", lng:" + mapvideodatalist[i].longitude)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.server_pin))
                                } else {
                                    userIndicator =
                                        MarkerOptions().position(LatLng(mapvideodatalist[i].latitude, mapvideodatalist[i].longitude)).draggable(true).title(mapvideodatalist[i].videoname).snippet("lat:" + mapvideodatalist[i].latitude.toString() + ", lng:" + mapvideodatalist[i].longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_normal_pin))
                                }
                                marker = map.addMarker(userIndicator)
                                markerList.add(marker)
                            }

                           /* val bounds = builder.build()
                            if (areBoundsTooSmall(bounds, 300)) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 17f))
                            } else {
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))
                            }*/



                            if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE) &&
                                SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE))
                            {

                                if(SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().isNotEmpty() &&
                                    SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE).toString().isNotEmpty()) {
                                    Log.d(
                                        "logger", "Current Lat Long==> " + LatLng(
                                            SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().toDouble(), SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE).toString().toDouble()
                                        )
                                    )

                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().toDouble(), SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE).toString().toDouble()
                                            ), 17f
                                        )
                                    )
                                }else{
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(
                                                markerList[0].position.latitude,markerList[0].position.longitude
                                            ), 17f
                                        )
                                    )
                                }
                            }


                            SharedPreferenceUtils.saveArrayList(mapvideodatalist, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)

                            Log.d("logger", "Final Map Video Size on api data getting==> " + mapvideodatalist.size)

                        } else {
                            wordViewModel.allWords.observe(this@MapViewFragment) { words -> // Update the cached copy of the words in the adapter.
                                words.let {
                                    val data = it
                                    uploaddatalistforserver = data as ArrayList<MarkerModel>
                                    if (!data.isNullOrEmpty()) {
                                        if (map != null) {
                                            map.clear()
                                            markerList.clear()
                                        }
                                        for (i in data.indices) {
                                            builder.include(LatLng(data[i].latitude, data[i].longitude)) //                                            createMarker(LatLng(data[i].latitude, data[i].longitude), data[i].videopath, LatLng(data[i].latitude, data[i].longitude).toString())
                                            var marker: Marker
                                            val userIndicator =
                                                MarkerOptions().position(LatLng(data[i].latitude, data[i].longitude)).draggable(true).title(data[i].videoname).snippet("lat:" + data[i].latitude.toString() + ", lng:" + data[i].longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_normal_pin))

                                            marker = map.addMarker(userIndicator)

                                            markerList.add(marker)
                                            mapvideodatalist.add(
                                                MarkerModel(
                                                    uploaddatalistforserver[i].id, uploaddatalistforserver[i].latitude, uploaddatalistforserver[i].longitude, uploaddatalistforserver[i].videopath, uploaddatalistforserver[i].videoname, uploaddatalistforserver[i].isserver
                                                )
                                            )
                                        }
                                        val bounds = builder.build()
                                        if (areBoundsTooSmall(bounds, 300)) {
                                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 17f))
                                        } else {
                                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))
                                        } //                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomLevel))

                                        SharedPreferenceUtils.saveArrayList(mapvideodatalist, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST) //                                        Log.d("logger", "Final Map Video Size ==> " + mapvideodatalist.size)
                                    } else {
                                        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE) && SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE)) {
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(
                                                        SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().toDouble(),
                                                        SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE).toString().toDouble()
                                                    ), 15f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        if (SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)) {
                            for (i in uploaddatalistforserver.indices) {
                                if (!uploaddatalistforserver[i].isserver) {
                                    startService()
                                    fireAndForgetNetworkCall()
                                    break
                                }
                            }

                        }


                    }
                    else -> {
                        val asString = event.asString(activity!!)
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                activity!!, asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = true
        if (ActivityCompat.checkSelfPermission(
                activity!!, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity!!, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        }
        map.isMyLocationEnabled = true
        map.isTrafficEnabled = true
        map.isBuildingsEnabled = true
        map.isIndoorEnabled = true
        map.uiSettings.isIndoorLevelPickerEnabled = true
        map.uiSettings.isCompassEnabled = true //        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity!!, R.raw.map_style))
        map.uiSettings.setAllGesturesEnabled(true)
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this)
        map.setOnMarkerDragListener(this)
    }

    private fun getdata() {
        markerList.clear()
        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_GUEST)) {
            if (!SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_GUEST, true)) {
                getApiData()
            } else {
                Log.d("logger", "when user in guest mode")
                wordViewModel.allWords.observe(activity!!) { words -> // Update the cached copy of the words in the adapter.
                    words.let {
                        val data = it
                        uploaddatalistforserver = data as ArrayList<MarkerModel>
                        if (!data.isNullOrEmpty()) {
                            if (map != null) {
                                map.clear()
                                markerList.clear()
                            }
                            val builder = LatLngBounds.Builder()
                            for (i in data.indices) {
                                builder.include(LatLng(data[i].latitude, data[i].longitude))
                                var marker: Marker
                                val userIndicator =
                                    MarkerOptions().position(LatLng(data[i].latitude, data[i].longitude)).title(data[i].videoname).draggable(true).snippet("lat:" + data[i].latitude.toString() + ", lng:" + data[i].longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_normal_pin))

                                marker = map.addMarker(userIndicator)

                                markerList.add(marker)

                                /*createMarker(
                                    LatLng(data[i].latitude, data[i].longitude), data[i].videopath, LatLng(data[i].latitude, data[i].longitude).toString()
                                )*/
                            }
                            val bounds = builder.build()
                            if (areBoundsTooSmall(bounds, 300)) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 17f))
                            } else {
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))
                            }
                            SharedPreferenceUtils.saveArrayList(uploaddatalistforserver, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)

                        } else {

                        }

                        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER)) {
                            if (SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)) {
                                startService()
                            }
                        }
                    }
                }
            }
        } else {
            Log.d("logger", "when user in guest mode")

            wordViewModel.allWords.observe(activity!!) { words -> // Update the cached copy of the words in the adapter.
                words.let {
                    val data = it
                    uploaddatalistforserver = data as ArrayList<MarkerModel>
                    if (!data.isNullOrEmpty()) {
                        if (map != null) {
                            map.clear()
                            markerList.clear()
                        }
                        for (i in data.indices) {
                            builder.include(LatLng(data[i].latitude, data[i].longitude))/*createMarker(
                                LatLng(data[i].latitude, data[i].longitude), data[i].videopath, LatLng(data[i].latitude, data[i].longitude).toString()
                            )*/

                            var marker: Marker
                            val userIndicator =
                                MarkerOptions().position(LatLng(data[i].latitude, data[i].longitude)).title(data[i].videoname).draggable(true).snippet("lat:" + data[i].latitude.toString() + ", lng:" + data[i].longitude).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_normal_pin))

                            marker = map.addMarker(userIndicator)

                            markerList.add(marker)
                        }
                        val bounds = builder.build()
                        if (areBoundsTooSmall(bounds, 300)) {
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 17f))
                        } else {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))
                        } //                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomLevel))      map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, zoomLevel))

                    } else {
                        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE) && SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.USER_CURRENT_LONGITUDE)) {
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().toDouble(), SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.USER_CURRENT_LATITUDE).toString().toDouble()
                                    ), 15f
                                )
                            )
                        }
                    }

                    SharedPreferenceUtils.saveArrayList(uploaddatalistforserver, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST) //                    Log.d("logger", "Final Map Video Size ==> " + mapvideodatalist.size)

                    if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER)) {
                        if (SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)) {
                            startService()
                        }
                    }
                }
            }
        }
    }

    open fun areBoundsTooSmall(bounds: LatLngBounds, minDistanceInMeter: Int): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result)
        return result[0] < minDistanceInMeter
    }

    override fun onResume() {
        super.onResume()
        Log.d("logger", "Token ==> " + SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.F_TOKEN).toString())
        getdata()


    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()

    }

    override fun onDetach() {
        super.onDetach()

    }

    open fun startService() {
        val serviceIntent = Intent(activity!!, ForegroundService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android") //        ContextCompat.startForegroundService(this, serviceIntent)
        activity!!.bindService(serviceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE)

    }

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.LocalBinder

            Log.d("service", "Service connected")
            createNotificationChannel()
            val notificationIntent = Intent(activity!!, MapActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                activity!!, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val notification: Notification =
                NotificationCompat.Builder(activity!!, ForegroundService.CHANNEL_ID).setContentTitle(getString(R.string.app_name)).setContentText("Video Uploading On Server").setSmallIcon(R.drawable.ic_normal_pin).setContentIntent(pendingIntent).build()
            binder.service.startForeground(1, notification)
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    fun fireAndForgetNetworkCall() {
        Log.i("logger", "-----Async network calls without error handling-----")
        lifecycleScope.executeAsyncTask(onPreExecute = { // ...
        }, doInBackground = { // ...
            "Result" // send data to "onPostExecute"
            if (mcount < uploaddatalistforserver.size) {
                if (!uploaddatalistforserver[mcount].isserver) {
                    Log.d("logger", "Video Uploading on server" + mcount)
                    loginViewModel.saveVideo(
                        uploaddatalistforserver[mcount].latitude, uploaddatalistforserver[mcount].longitude, uploaddatalistforserver[mcount].videoname, uploaddatalistforserver[mcount].videopath
                    )
                }
            }
        }, onPostExecute = { // ... here "it" is a data returned from "doInBackground"
            updateCount()
            lifecycleScope.launch {
                loginViewModel.allEventsFlow.collect { event ->
                    when (event) {
                        is AllEvents.SuccessBool -> {
                            when (event.code) {
                                1 -> {
                                    if (mcount < uploaddatalistforserver.size) {
                                        Log.d("logger", "Video Uploaded Sucessfully " + mcount)
                                        val memo = MarkerModel(
                                            id = uploaddatalistforserver[mcount].id, latitude = uploaddatalistforserver[mcount].latitude, longitude = uploaddatalistforserver[mcount].longitude, videopath = uploaddatalistforserver[mcount].videopath, videoname = uploaddatalistforserver[mcount].videoname, isserver = true
                                        )
                                        if (SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_REMOVE_FROM_DEVICE, false)) {
                                            wordViewModel.delete(memo)
                                            Log.d("logger", "Video Deleted Sucessfully becasue remove device is on")
                                        } else {
                                            Log.d("logger", "Video Update Sucessfully becasue remove device is off")
                                            wordViewModel.update(memo)
                                        }
                                        updateCount()
                                    } else {
                                        Log.d("logger", "all Video Uploaded Sucessfully")
                                        mcount = 0
                                    }
                                }
                            }
                        }
                        else -> {
                            val asString = event.asString(activity!!)
                            if (asString !is Unit && asString.toString().isNotBlank()) {
                                Toast.makeText(
                                    activity!!, asString.toString(), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

        })

    }

    private fun updateCount() {
        if (uploaddatalistforserver.size != 0) {
            if (uploaddatalistforserver[mcount].isserver && mcount < uploaddatalistforserver.size - 1) {
                mcount += 1
                fireAndForgetNetworkCall()
            }
        }
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
            val manager = activity!!.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    open fun stopService() {
        val serviceIntent = Intent(activity!!, ForegroundService::class.java)
        activity!!.stopService(serviceIntent)
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
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showMessageOKCancel(
                            "You need to allow access to both the permissions"
                        ) { _, _ ->
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
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
        AlertDialog.Builder(activity!!).setMessage(message).setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show()
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val intent =
            Intent(activity!!, VideoActivity::class.java).putExtra("VideoPath", marker?.title)
        startActivity(intent)
        return true
    }

    override fun onMapLongClick(marker: LatLng?) { //        Toast.makeText(this, "${marker?.latitude} ${marker?.longitude}", Toast.LENGTH_SHORT).show()
        val snackbar = Snackbar.make(
            fragmentmapBinding.mainlayout, "Latitude:${marker?.latitude} Longitude:${marker?.longitude}", Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }

    var onDragposition = 0
    override fun onMarkerDragEnd(p0: Marker?) {
        Log.d("marker End", "End Lat Long ==>" + p0?.position.toString())
        for (i in mapvideodatalist.indices) {
            if (mapvideodatalist[i].videoname == p0?.title)
            {
                if(mapvideodatalist[i].isserver){
                    loginViewModel.editMarker(EditMarkerRequestModel(id = mapvideodatalist[i].id.toString(), name = mapvideodatalist[i].videoname,
                    lat = p0?.position?.latitude?.toDouble().toString(), long = p0?.position?.longitude?.toDouble().toString()))
                }else {
                    val memo = MarkerModel(
                        id = mapvideodatalist[i].id, latitude = p0?.position?.latitude?.toDouble()!!, longitude = p0?.position?.longitude?.toDouble()!!, videopath = mapvideodatalist[i].videopath, videoname = mapvideodatalist[i].videoname, isserver = true
                    )
                    Log.d("logger", "Lat/Long Update Sucessfully in Database")
                    wordViewModel.update(memo)
                }
                onDragposition = i
                break
            }
        }

        lifecycleScope.launch {
            loginViewModel.allEventsFlow.collect { event ->
                when (event) {
                    is AllEvents.SuccessBool -> {
                        when (event.code) {
                            1 -> {
                                val memo = MarkerModel(
                                    id = mapvideodatalist[onDragposition].id, latitude = p0?.position?.latitude?.toDouble()!!,
                                    longitude = p0?.position?.longitude?.toDouble()!!,
                                    videopath = mapvideodatalist[onDragposition].videopath, videoname = mapvideodatalist[onDragposition].videoname, isserver = true
                                )
                                Log.d("logger", "update in database & api ")
                                wordViewModel.update(memo)
                                onDragposition = 0
                            }
                        }
                    }
                    else -> {
                        val asString = event.asString(activity!!)
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                activity!!, asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}


