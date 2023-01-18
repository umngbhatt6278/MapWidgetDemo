package com.example.mapwidgetdemo.custom_camera

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.hardware.SensorManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.ExifInterface
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.custom_camera.constants.Constants
import com.example.mapwidgetdemo.custom_camera.util.GLUtil
import com.example.mapwidgetdemo.custom_camera.util.MediaUtil
import com.example.mapwidgetdemo.custom_camera.util.SDCardUtil
import com.example.mapwidgetdemo.custom_camera.view.CameraView
import com.example.mapwidgetdemo.custom_camera.view.PinchZoomGestureListener
import com.example.mapwidgetdemo.ui.activity.REQUEST_LOCATION_PERMISSION
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.DialogClickInterface
import com.example.mapwidgetdemo.utils.DialogUtils
import com.example.mapwidgetdemo.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException
import java.util.*


class VideoFragment : Fragment() {
    var zoomBar: SeekBar? = null
    var cameraView: CameraView? = null
    var switchCamera: ImageButton? = null
    var startRecord: ImageButton? = null
    var flash: ImageButton? = null
    var photoMode: ImageButton? = null
    var substitute: ImageView? = null
    var thumbnail: ImageView? = null
    var settings: ImageButton? = null
    var videoBar: LinearLayout? = null
    var settingsBar: LinearLayout? = null
    var timeElapsed: TextView? = null
    var memoryConsumed: TextView? = null
    var permissionInterface: PermissionInterface? = null
    var switchInterface: SwitchInterface? = null
    var lowestThresholdCheckForVideoInterface: LowestThresholdCheckForVideoInterface? = null
    var stopRecord: ImageButton? = null

    //    var imagePreview: ImageView? = null
    var pauseRecord: ImageButton? = null
    var modeText: TextView? = null
    var resInfo: TextView? = null
    var modeLayout: LinearLayout? = null
    var orientationEventListener: OrientationEventListener? = null
    var orientation = -1
    var flashParentLayout: LinearLayout? = null
    var timeElapsedParentLayout: LinearLayout? = null
    var memoryConsumedParentLayout: LinearLayout? = null
    var parentLayoutParams: LinearLayout.LayoutParams? = null
    var thumbnailParent: FrameLayout? = null
    var exifInterface: ExifInterface? = null
    var warningMsgRoot: View? = null
    var warningMsg: Dialog? = null
    internal var layoutInflater: LayoutInflater? = null
    var sdCardEventReceiver: SDCardEventReceiver? = null
    var mediaFilters: IntentFilter? = null
    var okButton: Button? = null
    var pauseText: TextView? = null
    var sdCardUnavailWarned = false
    var sharedPreferences: SharedPreferences? = null
    var microThumbnail: ImageView? = null
    var appWidgetManager: AppWidgetManager? = null
    var VERBOSE = false
    var isPause = false
    var settingsMsgRoot: View? = null
    var settingsMsgDialog: Dialog? = null
    var applicationContext: Context? = null
    var pinchZoomGestureListener: PinchZoomGestureListener? = null
    var audioSampleRate = -1
    var audioBitRate = -1
    var audioChannelInput = -1
    var controlVisbilityPreference: ControlVisbilityPreference? = null


    open var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationManager: LocationManager? = null
    private val minTime: Long = 10000
    private val minDistance = 10f

    private var isGPSEnabled = false


    interface PermissionInterface {
        fun askPermission()
    }

    interface SwitchInterface {
        fun switchToPhoto()
    }

    interface LowestThresholdCheckForVideoInterface {
        fun checkIfPhoneMemoryIsBelowLowestThresholdForVideo(): Boolean
    }

    var cameraActivity: CameraActivity? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (VERBOSE) Log.d(TAG, "onActivityCreated")
        if (cameraView != null) {
            cameraView!!.setWindowManager(requireActivity().windowManager)
        }
        cameraActivity = activity as CameraActivity?
        settingsBar = cameraActivity!!.findViewById<View>(R.id.settingsBar) as LinearLayout
        settings = cameraActivity!!.findViewById<View>(R.id.settings) as ImageButton
        flash = cameraActivity!!.findViewById<View>(R.id.flashOn) as ImageButton
        flash!!.setOnClickListener { setFlash() }
        cameraView!!.setFlashButton(flash)
        modeText = cameraActivity!!.findViewById<View>(R.id.modeInfo) as TextView
        resInfo = cameraActivity!!.findViewById<View>(R.id.resInfo) as TextView
        modeLayout = cameraActivity!!.findViewById<View>(R.id.modeLayout) as LinearLayout
        permissionInterface = cameraActivity
        switchInterface = cameraActivity
        lowestThresholdCheckForVideoInterface = cameraActivity
        layoutInflater =
            cameraActivity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        warningMsgRoot = layoutInflater!!.inflate(R.layout.warning_message, null)
        warningMsg = Dialog(cameraActivity!!)
        settingsMsgRoot = layoutInflater!!.inflate(R.layout.settings_message, null)
        settingsMsgDialog = Dialog(cameraActivity!!)
        mediaFilters = IntentFilter()
        sdCardEventReceiver = SDCardEventReceiver()
        sharedPreferences =
            cameraActivity!!.getSharedPreferences(Constants.FC_SETTINGS, Context.MODE_PRIVATE)
        appWidgetManager =
            cameraActivity!!.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
        pinchZoomGestureListener = cameraActivity!!.pinchZoomGestureListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    private fun startLocationUpdates() {

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!

        when {
            !isGPSEnabled -> { //
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            else -> {
                getLocation()
            }
        }
    }


    private fun getLocation() {
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = true
        criteria.isCostAllowed = true
        criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
        criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH

        val providers = locationManager!!.getProviders(criteria, true)
        for (provider in providers) {
            if (!provider.contains("gps")) { // if gps is disabled
                val poke = Intent()
                poke.setClassName(
                    "com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"
                )
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
                poke.data = Uri.parse("3")
                requireActivity().sendBroadcast(poke)
            } // Get the location from the given provider
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            } else {
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener
                )
            }
        }

        var handler = Handler(Looper.myLooper()!!)
        handler.postDelayed(Runnable {
            kotlin.run {
                startRecord?.performClick()
            }
        }, 500)
    }

    private val locationListener = LocationListener {
        currentLatitude = it.latitude
        currentLongitude = it.longitude
        Toast.makeText(requireActivity(), "Cur Lat/Long$currentLatitude,$currentLongitude", Toast.LENGTH_SHORT).show();
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
            ), REQUEST_LOCATION_PERMISSION
        )
    }

    val cameraMaxZoom: Int
        get() = cameraView!!.cameraMaxZoom

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        if (VERBOSE) Log.d(TAG, "Inside video fragment")
        substitute = view.findViewById<View>(R.id.substitute) as ImageView
        substitute!!.visibility = View.INVISIBLE
        cameraView = view.findViewById<View>(R.id.cameraSurfaceView) as CameraView
        GLUtil.colorVal = Constants.NORMAL_BRIGHTNESS_PROGRESS
        if (VERBOSE) Log.d(TAG, "cameraview onresume visibility= " + cameraView!!.windowVisibility)
        pauseText = view.findViewById(R.id.pauseText)
        zoomBar = view.findViewById<View>(R.id.zoomBar) as SeekBar
        zoomBar!!.progressTintList =
            ColorStateList.valueOf(resources.getColor(R.color.progressFill))
        cameraView!!.setSeekBar(zoomBar)
        zoomBar!!.progress = 0
        zoomBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (cameraView!!.isCameraReady && fromUser) {
                    if (cameraView!!.isSmoothZoomSupported) { //if(VERBOSE)Log.d(TAG, "Smooth zoom supported");
                        cameraView!!.smoothZoomInOrOut(progress)
                    } else if (cameraView!!.isZoomSupported) {
                        cameraView!!.zoomInAndOut(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (!cameraView!!.isSmoothZoomSupported && !cameraView!!.isZoomSupported) {
                    Toast.makeText(activity!!.applicationContext, resources.getString(R.string.zoomNotSupported), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (VERBOSE) Log.d(TAG, "onStopTrackingTouch = " + seekBar.progress)
                cameraActivity!!.pinchZoomGestureListener!!.setProgress(seekBar.progress)
            }
        })
        thumbnail = view.findViewById<View>(R.id.thumbnail) as ImageView
        microThumbnail = view.findViewById<View>(R.id.microThumbnail) as ImageView
        thumbnailParent = view.findViewById<View>(R.id.thumbnailParent) as FrameLayout
        photoMode = view.findViewById<View>(R.id.photoMode) as ImageButton
        photoMode!!.setOnClickListener { switchInterface!!.switchToPhoto() }
        switchCamera = view.findViewById<View>(R.id.switchCamera) as ImageButton
        switchCamera!!.setOnClickListener {
            startRecord!!.isClickable = false
            flash!!.isClickable = false
            photoMode!!.isClickable = false
            thumbnail!!.isClickable = false
            settings!!.isClickable = false
            cameraView!!.switchCamera()
            zoomBar!!.progress = 0
            cameraActivity!!.pinchZoomGestureListener!!.setProgress(0)
            zoomBar!!.progress = 0
            startRecord!!.isClickable = true
            flash!!.isClickable = true
            photoMode!!.isClickable = true
            thumbnail!!.isClickable = true
            settings!!.isClickable = true
        }
        startRecord = view.findViewById<View>(R.id.cameraRecord) as ImageButton
        videoBar = view.findViewById<View>(R.id.videoFunctions) as LinearLayout


        startRecord!!.setOnClickListener {
            startRecord!!.isClickable = false
            switchCamera!!.isClickable = false
            photoMode!!.isClickable = false
            thumbnail!!.isClickable = false
            if (lowestThresholdCheckForVideoInterface!!.checkIfPhoneMemoryIsBelowLowestThresholdForVideo()) {
                val layoutInflater =
                    requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val thresholdExceededRoot =
                    layoutInflater.inflate(R.layout.threshold_exceeded, null)
                val thresholdDialog = Dialog(requireActivity())
                val memoryLimitMsg =
                    thresholdExceededRoot.findViewById<View>(R.id.memoryLimitMsg) as TextView
                val lowestThreshold = resources.getInteger(R.integer.minimumMemoryWarning)
                val minimumThreshold = StringBuilder(lowestThreshold.toString() + "")
                minimumThreshold.append(" ")
                minimumThreshold.append(resources.getString(R.string.MEM_PF_MB))
                if (VERBOSE) Log.d(TAG, "minimumThreshold = $minimumThreshold")
                memoryLimitMsg.text =
                    resources.getString(R.string.minimumThresholdExceeded, minimumThreshold)
                val disableThreshold =
                    thresholdExceededRoot.findViewById<View>(R.id.disableThreshold) as CheckBox
                disableThreshold.visibility = View.GONE
                val okButton = thresholdExceededRoot.findViewById<View>(R.id.okButton) as Button
                okButton.setOnClickListener {
                    thresholdDialog.dismiss()
                    startRecord!!.isClickable = true
                    photoMode!!.isClickable = true
                    thumbnail!!.isClickable = true
                    switchCamera!!.isClickable = true
                }
                thresholdDialog.setContentView(thresholdExceededRoot)
                thresholdDialog.setCancelable(false)
                thresholdDialog.show()
            } else {
                val settingsEditor = sharedPreferences!!.edit()
                if (sharedPreferences!!.getBoolean(Constants.PHONE_MEMORY_DISABLE, true)) {
                    if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) { //Check if the FC folder exists inside SD Card.
                        if (SDCardUtil.doesSDCardFlipCamFolderExist(sharedPreferences!!.getString(Constants.SD_CARD_PATH, ""))) {
                            sdCardUnavailWarned = false
                            prepareAndStartRecord()
                        } else { //If the FC Folder does not exist, create a new folder and continue recording.
                            if (SDCardUtil.doesSDCardExist(applicationContext) == null) {
                                sdCardUnavailWarned = true
                                settingsEditor.putBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)
                                settingsEditor.commit()
                                showErrorWarningMessage(resources.getString(R.string.sdCardRemovedTitle), resources.getString(R.string.sdCardNotPresentForRecord))
                                latestFileIfExists
                            } else { //Continue recording. doesSDCardExist() will create a new folder which can be used for recording.
                                sdCardUnavailWarned = false
                                prepareAndStartRecord()
                            }
                        }
                    } else {
                        prepareAndStartRecord()
                    }
                } else {
                    checkIfMemoryLimitIsExceeded()
                }
            }
        }
        if (VERBOSE) Log.d(TAG, "passing videofragment to cameraview")
        cameraView!!.setFragmentInstance(this)
        cameraView!!.setPhotoFragmentInstance(null) //        imagePreview = view.findViewById(R.id.imagePreview) as ImageView
        orientationEventListener = object :
            OrientationEventListener(requireActivity().applicationContext, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(i: Int) {
                if (orientationEventListener!!.canDetectOrientation()) {
                    orientation = i
                    determineOrientation()
                    rotateIcons()
                }
            }
        }
        flashParentLayout = LinearLayout(activity)
        timeElapsedParentLayout = LinearLayout(activity)
        memoryConsumedParentLayout = LinearLayout(activity)
        parentLayoutParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        parentLayoutParams!!.weight = 1f
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val mediaCodecInfos = mediaCodecList.codecInfos
        if (audioBitRate == -1 || audioChannelInput == -1 || audioSampleRate == -1) {
            for (info in mediaCodecInfos) {
                if (VERBOSE) Log.d(TAG, "Name = " + info.name)
                if (info.name.contains("aac")) {
                    val medTypes = info.supportedTypes
                    for (medType in medTypes) {
                        if (VERBOSE) Log.d(TAG, "media types = $medType")
                        if (medType.contains("mp4a") || medType.contains("mp4") || medType.contains("mpeg4")) {
                            val audioCapabilities =
                                info.getCapabilitiesForType(medType).audioCapabilities
                            val bitRates = audioCapabilities.bitrateRange
                            if (VERBOSE) Log.d(TAG, "Bit rate range = " + bitRates.lower + " , " + bitRates.upper)
                            audioBitRate = bitRates.upper
                            val sampleRates = audioCapabilities.supportedSampleRates
                            Arrays.sort(sampleRates)
                            if (VERBOSE) Log.d(TAG, "Sample rate = " + sampleRates[sampleRates.size - 1])
                            audioSampleRate = sampleRates[sampleRates.size - 1]
                            audioChannelInput =
                                if (audioCapabilities.maxInputChannelCount > 1) 2 else 1
                            break
                        }
                    }
                    break
                }
            }
        }
        controlVisbilityPreference = applicationContext as ControlVisbilityPreference?
        return view
    }

    fun autoStart() {
        Toast.makeText(requireActivity(), "Recording Start", Toast.LENGTH_SHORT).show();
        startRecord!!.isClickable = true
        photoMode!!.isClickable = true
        thumbnail!!.isClickable = true
        switchCamera!!.isClickable = true
        videoBar!!.removeAllViews()
        addStopAndPauseIcons()
        hideSettingsBarAndIcon()
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("videoCapture", true)
        editor.commit()
        cameraView!!.record(false)
    }

    inner class SDCardEventReceiver : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (VERBOSE) Log.d(TAG, "onReceive = " + intent.action)
            if (intent.action.equals(Intent.ACTION_MEDIA_UNMOUNTED, ignoreCase = true) || intent.action.equals(Constants.MEDIA_UNMOUNTED, ignoreCase = true)) { //Check if SD Card was selected
                val settingsEditor = sharedPreferences!!.edit()
                if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true) && !sdCardUnavailWarned) {
                    if (VERBOSE) Log.d(TAG, "SD Card Removed")
                    settingsEditor.putBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)
                    settingsEditor.commit()
                    showErrorWarningMessage(resources.getString(R.string.sdCardRemovedTitle), resources.getString(R.string.sdCardNotPresentForRecord))
                    latestFileIfExists
                }
            }
        }
    }

    fun showToastSDCardUnavailWhileRecordMessage() {
        Toast.makeText(applicationContext, resources.getString(R.string.sdCardRemovedWhileRecord), Toast.LENGTH_LONG).show()
    }

    fun showErrorWarningMessage(title: String?, message: String?) {
        val warningTitle = warningMsgRoot!!.findViewById<View>(R.id.warningTitle) as TextView
        warningTitle.text = title
        val warningText = warningMsgRoot!!.findViewById<View>(R.id.warningText) as TextView
        warningText.text = message
        okButton = warningMsgRoot!!.findViewById<View>(R.id.okButton) as Button
        okButton!!.setOnClickListener { view: View? ->
            startRecord!!.isClickable = true
            photoMode!!.isClickable = true
            thumbnail!!.isClickable = true
            switchCamera!!.isClickable = true
            warningMsg!!.dismiss()
        }
        warningMsg!!.setContentView(warningMsgRoot!!)
        warningMsg!!.setCancelable(false)
        warningMsg!!.show()
    }

    fun checkForSDCard() {
        if (VERBOSE) Log.d(TAG, "save media pref = " + sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true))
        if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) {
            if (!SDCardUtil.doesSDCardFlipCamFolderExist(sharedPreferences!!.getString(Constants.SD_CARD_PATH, ""))) {
                if (VERBOSE) Log.d(TAG, "FC Folder not exist SD Card")
                if (VERBOSE) Log.d(TAG, "showFCFolderNotExistMessage")
                showErrorWarningMessage(resources.getString(R.string.sdCardFCFolderNotExistTitle), resources.getString(R.string.sdCardFCFolderNotExistMessage))
            }
        } else {
            if (VERBOSE) Log.d(TAG, "displaySDCardNotDetectMessage 2222")
            cameraActivity!!.displaySDCardNotDetectMessage()
        }
    }

    private val isUseFCPlayer: Boolean
        private get() {
            val fcPlayer = resources.getString(R.string.videoFCPlayer)
            val externalPlayer = resources.getString(R.string.videoExternalPlayer)
            val videoPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return videoPrefs.getString(Constants.SELECT_VIDEO_PLAYER, externalPlayer).equals(fcPlayer, ignoreCase = true)
        }

    fun prepareAndStartRecord() {
        val audioManager = cameraView!!.audioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (VERBOSE) Log.d(TAG, "setStreamMute")
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        } else {
            if (VERBOSE) Log.d(TAG, "adjustStreamVolume")
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        }
        startRecord!!.isClickable = true
        photoMode!!.isClickable = true
        thumbnail!!.isClickable = true
        switchCamera!!.isClickable = true
        videoBar!!.removeAllViews()
        addStopAndPauseIcons()
        hideSettingsBarAndIcon()
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("videoCapture", true)
        editor.commit()
        cameraView!!.record(false)
    }

    fun checkIfMemoryLimitIsExceeded() {
        val editor = sharedPreferences!!.edit()
        val memoryThreshold =
            sharedPreferences!!.getString(Constants.PHONE_MEMORY_LIMIT, "")!!.toInt()
        val memoryMetric = sharedPreferences!!.getString(Constants.PHONE_MEMORY_METRIC, "")
        val storageStat = StatFs(Environment.getDataDirectory().path)
        var memoryValue: Long = 0
        var metric = ""
        when (memoryMetric) {
            "MB" -> {
                memoryValue = memoryThreshold * Constants.MEGA_BYTE.toLong()
                metric = "MB"
            }
            "GB" -> {
                memoryValue = memoryThreshold * Constants.GIGA_BYTE.toLong()
                metric = "GB"
            }
        }
        if (VERBOSE) Log.d(TAG, "memory value = $memoryValue")
        if (VERBOSE) Log.d(TAG, "Avail mem = " + storageStat.availableBytes)
        if (storageStat.availableBytes < memoryValue) {
            val layoutInflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val thresholdExceededRoot = layoutInflater.inflate(R.layout.threshold_exceeded, null)
            val thresholdDialog = Dialog(requireActivity())
            val memoryLimitMsg =
                thresholdExceededRoot.findViewById<View>(R.id.memoryLimitMsg) as TextView
            val disableThreshold =
                thresholdExceededRoot.findViewById<View>(R.id.disableThreshold) as CheckBox
            val okButton = thresholdExceededRoot.findViewById<View>(R.id.okButton) as Button
            okButton.setOnClickListener {
                if (VERBOSE) Log.d(TAG, "disableThreshold.isChecked = " + disableThreshold.isChecked)
                if (disableThreshold.isChecked) {
                    editor.remove(Constants.PHONE_MEMORY_LIMIT)
                    editor.remove(Constants.PHONE_MEMORY_METRIC)
                    editor.putBoolean(Constants.PHONE_MEMORY_DISABLE, true)
                    editor.commit()
                    Toast.makeText(applicationContext, resources.getString(R.string.minimumThresholdDisabled), Toast.LENGTH_LONG).show()
                }
                thresholdDialog.dismiss()
                prepareAndStartRecord()
            }
            val memThreshold = StringBuilder(memoryThreshold.toString() + "")
            memThreshold.append(" ")
            memThreshold.append(metric)
            if (VERBOSE) Log.d(TAG, "memory threshold for display = $memThreshold")
            memoryLimitMsg.text =
                requireActivity().resources.getString(R.string.thresholdLimitExceededMsg, memThreshold.toString())
            thresholdDialog.setContentView(thresholdExceededRoot)
            thresholdDialog.setCancelable(false)
            thresholdDialog.show()
        } else {
            prepareAndStartRecord()
        }
    }

    fun rotateIcons() {
        switchCamera!!.rotation = rotationAngle
        photoMode!!.rotation = rotationAngle
        flash!!.rotation = rotationAngle
        microThumbnail!!.rotation = rotationAngle
        if (pauseRecord != null) {
            pauseRecord!!.rotation = rotationAngle
            pauseText!!.rotation = rotationAngle
        }
        if (exifInterface != null && !filePath.equals("", ignoreCase = true)) {
            if (isImage(filePath)) {
                if (exifInterface!!.getAttribute(ExifInterface.TAG_ORIENTATION).equals(ExifInterface.ORIENTATION_ROTATE_90.toString(), ignoreCase = true)) {
                    rotationAngle += 90f
                } else if (exifInterface!!.getAttribute(ExifInterface.TAG_ORIENTATION).equals(ExifInterface.ORIENTATION_ROTATE_270.toString(), ignoreCase = true)) {
                    rotationAngle += 270f
                }
            }
        }
        thumbnail!!.rotation = rotationAngle
    }

    fun addStopAndPauseIcons() {
        videoBar!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        val layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        stopRecord = ImageButton(requireActivity().applicationContext)
        stopRecord!!.scaleType = ImageView.ScaleType.CENTER_CROP
        stopRecord!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        stopRecord!!.setImageDrawable(resources.getDrawable(R.drawable.camera_record_stop))
        cameraView!!.setStopButton(stopRecord)
        layoutParams.height = resources.getDimension(R.dimen.stopButtonHeight).toInt()
        layoutParams.width = resources.getDimension(R.dimen.stopButtonWidth).toInt()
        layoutParams.setMargins(resources.getDimension(R.dimen.stopBtnLeftMargin).toInt(), 0, resources.getDimension(R.dimen.stopBtnRightMargin).toInt(), 0)
        stopRecord!!.layoutParams = layoutParams
        stopRecord!!.setOnClickListener { stopRecordAndSaveFile(false) }
        switchCamera!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        switchCamera!!.rotation = rotationAngle
        videoBar!!.addView(switchCamera)
        videoBar!!.addView(stopRecord) //        addPauseButton()
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun addPauseButton() {
        pauseRecord = ImageButton(requireActivity().applicationContext)
        pauseRecord!!.scaleType = ImageView.ScaleType.CENTER_CROP
        pauseRecord!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        pauseRecord!!.setImageDrawable(resources.getDrawable(R.drawable.camera_record_pause))
        pauseRecord!!.setOnClickListener {
            pauseRecord!!.isEnabled = false
            if (VERBOSE) Log.d(TAG, "isPause ==== $isPause")
            isPause = if (!isPause) {
                cameraView!!.recordPause()
                pauseRecord!!.setImageDrawable(resources.getDrawable(R.drawable.camera_record_resume))
                true
            } else {
                cameraView!!.recordResume()
                pauseRecord!!.setImageDrawable(resources.getDrawable(R.drawable.camera_record_pause))
                false
            }
            pauseRecord!!.isEnabled = true
        }
        val layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 0, resources.getDimension(R.dimen.recordSubsBtnRightMargin).toInt(), 0)
        layoutParams.width = resources.getDimension(R.dimen.pauseButtonWidth).toInt()
        layoutParams.height = resources.getDimension(R.dimen.pauseButtonHeight).toInt()
        pauseRecord!!.layoutParams = layoutParams
        videoBar!!.addView(pauseRecord)
    }

    fun stopRecordAndSaveFile(lowMemory: Boolean) {
        var noSdCard = false
        stopRecord!!.isClickable = false
        switchCamera!!.isClickable = false
        if (VERBOSE) Log.d(TAG, "Unmute audio stopRec")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (VERBOSE) Log.d(TAG, "setStreamUnMute")
            cameraView!!.audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
        } else {
            if (VERBOSE) Log.d(TAG, "adjustStreamVolumeUnMute")
            cameraView!!.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        }
        if (lowMemory) {
            val layoutInflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val thresholdExceededRoot = layoutInflater.inflate(R.layout.threshold_exceeded, null)
            val thresholdDialog = Dialog(requireActivity())
            val lowestThreshold = resources.getInteger(R.integer.minimumMemoryWarning)
            val memoryLimitMsg =
                thresholdExceededRoot.findViewById<View>(R.id.memoryLimitMsg) as TextView
            val minimumThreshold = StringBuilder(lowestThreshold.toString() + "")
            minimumThreshold.append(" ")
            minimumThreshold.append(resources.getString(R.string.MEM_PF_MB))
            memoryLimitMsg.text =
                resources.getString(R.string.minimumThresholdExceeded, minimumThreshold)
            val disableThreshold =
                thresholdExceededRoot.findViewById<View>(R.id.disableThreshold) as CheckBox
            disableThreshold.visibility = View.GONE
            val okButton = thresholdExceededRoot.findViewById<View>(R.id.okButton) as Button
            okButton.setOnClickListener {
                thresholdDialog.dismiss()
                stopRecord!!.isClickable = true
                switchCamera!!.isClickable = true
            }
            thresholdDialog.setContentView(thresholdExceededRoot)
            thresholdDialog.setCancelable(false)
            thresholdDialog.show()
            addMediaToDB()
        } else {
            if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) {
                if (SDCardUtil.doesSDCardExist(applicationContext) != null) {
                    noSdCard = false
                } else {
                    startRecord!!.isClickable = false
                    photoMode!!.isClickable = false
                    thumbnail!!.isClickable = false
                    switchCamera!!.isClickable = false
                    noSdCard = true
                    val settingsEditor = sharedPreferences!!.edit()
                    settingsEditor.putBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)
                    settingsEditor.commit()
                    showErrorWarningMessage(resources.getString(R.string.sdCardRemovedTitle), resources.getString(R.string.sdCardRemovedWhileRecord))
                    latestFileIfExists
                    Thread { deleteLatestBadFile() }.start()
                }
            } else {
                noSdCard = false
            }
            cameraView!!.record(noSdCard)
        }
        showRecordAndThumbnail()
        stopRecord!!.isClickable = true
        switchCamera!!.isClickable = true

    /*if(sharedPreferences.getBoolean(Constants.SAVE_TO_GOOGLE_DRIVE, false) && !noSdCard) {
            if(VERBOSE)Log.d(TAG, "Auto uploading to Google Drive");
            //Auto upload to Google Drive enabled.
            Intent googleDriveUploadIntent = new Intent(getApplicationContext(), GoogleDriveUploadService.class);
            googleDriveUploadIntent.putExtra("uploadFile", cameraView.getMediaPath());
            if(VERBOSE)Log.d(TAG, "Uploading file = "+cameraView.getMediaPath());
            getActivity().startService(googleDriveUploadIntent);
        }
        if(sharedPreferences.getBoolean(Constants.SAVE_TO_DROPBOX, false) && !noSdCard){
            if(VERBOSE)Log.d(TAG, "Auto upload to Dropbox");
            //Auto upload to Dropbox enabled
            Intent dropboxUploadIntent = new Intent(getApplicationContext(), DropboxUploadService.class);
            dropboxUploadIntent.putExtra("uploadFile", cameraView.getMediaPath());
            if(VERBOSE)Log.d(TAG, "Uploading file = "+cameraView.getMediaPath());
            getActivity().startService(dropboxUploadIntent);
        }*/
    }

    var rotationAngle = 0f
    fun determineOrientation() {
        if (orientation != -1) {
            rotationAngle =
                if (orientation >= 315 && orientation <= 360 || orientation >= 0 && orientation <= 45 || orientation >= 135 && orientation <= 195) {
                    if (orientation >= 135 && orientation <= 195) { //Reverse portrait
                        180f
                    } else { //Portrait
                        0f
                    }
                } else {
                    if (orientation >= 46 && orientation <= 134) { //Reverse Landscape
                        270f
                    } else { //Landscape
                        90f
                    }
                }
        }
    }

    fun showRecordSaved() {
        val recordSavedLayout = LinearLayout(activity)
        recordSavedLayout.gravity = Gravity.CENTER
        recordSavedLayout.orientation = LinearLayout.VERTICAL
        recordSavedLayout.setBackgroundColor(resources.getColor(R.color.savedMsg))
        determineOrientation()
        recordSavedLayout.rotation = rotationAngle
        val recordSavedText = TextView(activity)
        recordSavedText.text = resources.getString(R.string.RECORD_SAVED)
        val recordSavedImg = ImageView(activity)
        recordSavedImg.setImageDrawable(resources.getDrawable(R.drawable.ic_done_white))
        recordSavedText.setPadding(resources.getDimension(R.dimen.recordSavePadding).toInt(), resources.getDimension(R.dimen.recordSavePadding).toInt(), resources.getDimension(R.dimen.recordSavePadding).toInt(), resources.getDimension(R.dimen.recordSavePadding).toInt())
        recordSavedText.setTextColor(resources.getColor(R.color.saveText))
        recordSavedImg.setPadding(0, 0, 0, resources.getDimension(R.dimen.recordSaveImagePaddingBottom).toInt())
        recordSavedLayout.addView(recordSavedText)
        recordSavedLayout.addView(recordSavedImg)
        val showCompleted =
            Toast.makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)
        showCompleted.setGravity(Gravity.CENTER, 0, 0)
        showCompleted.view = recordSavedLayout
        showCompleted.show()
        Thread {
            try {
                Thread.sleep(1000)
                showCompleted.cancel()
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
            }
        }.start()
    }

    fun showRecordAndThumbnail() {
        videoBar!!.setBackgroundColor(resources.getColor(R.color.settingsBarColor))
        videoBar!!.removeAllViews()
        videoBar!!.addView(substitute)
        videoBar!!.addView(switchCamera)
        videoBar!!.addView(startRecord)
        videoBar!!.addView(photoMode)
        videoBar!!.addView(thumbnailParent)
        settingsBar!!.removeAllViews()
        settingsBar!!.weightSum = 0f
        flashParentLayout!!.removeAllViews()
        timeElapsedParentLayout!!.removeAllViews()
        memoryConsumedParentLayout!!.removeAllViews()
        if (cameraView!!.isCameraReady) {
            if (cameraView!!.isFlashOn) {
                flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_off))
            } else {
                flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_on))
            }
        }
        val flashParams =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flashParams.weight = 0.5f
        flashParams.height = resources.getDimension(R.dimen.flashOnHeight).toInt()
        flashParams.width = resources.getDimension(R.dimen.flashOnWidth).toInt()
        flashParams.setMargins(resources.getDimension(R.dimen.flashOnLeftMargin).toInt(), 0, 0, 0)
        flashParams.gravity = Gravity.CENTER
        flash!!.scaleType = ImageView.ScaleType.FIT_CENTER
        flash!!.layoutParams = flashParams
        flash!!.setOnClickListener { setFlash() }
        settingsBar!!.addView(flash)
        cameraView!!.setFlashButton(flash)
        settingsBar!!.addView(modeLayout)
        settingsBar!!.addView(settings)
        modeText!!.text = resources.getString(R.string.VIDEO_MODE)
        settingsBar!!.setBackgroundColor(resources.getColor(R.color.settingsBarColor))
        flash!!.setBackgroundColor(resources.getColor(R.color.settingsBarColor))
    }

    fun setVideoResInfo(width: String?, height: String?) {
        resInfo!!.text = resources.getString(R.string.resolutionDisplay, width, height)
    }

    fun hideSettingsBarAndIcon() {
        settingsBar!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        settingsBar!!.removeAllViews()
        flashParentLayout!!.removeAllViews()
        timeElapsedParentLayout!!.removeAllViews()
        memoryConsumedParentLayout!!.removeAllViews()
        settingsBar!!.weightSum = 3f
        flashParentLayout!!.layoutParams = parentLayoutParams
        if (cameraView!!.isFlashOn) {
            flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_off))
        } else {
            flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_on))
        }
        flash!!.setOnClickListener { setFlash() }
        val flashParam =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flashParam.weight = 1f
        flashParam.setMargins(0, resources.getDimension(R.dimen.flashOnTopMargin).toInt(), 0, 0)
        flashParam.width = resources.getDimension(R.dimen.flashOnWidth).toInt()
        flashParam.height = resources.getDimension(R.dimen.flashOnHeight).toInt()
        flash!!.scaleType = ImageView.ScaleType.FIT_CENTER
        flash!!.layoutParams = flashParam
        flash!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        cameraView!!.setFlashButton(flash)
        flashParentLayout!!.addView(flash)
        settingsBar!!.addView(flashParentLayout)

        //Add time elapsed text
        timeElapsed = TextView(activity)
        timeElapsed!!.gravity = Gravity.CENTER_HORIZONTAL
        timeElapsed!!.typeface = Typeface.DEFAULT_BOLD
        timeElapsed!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        timeElapsed!!.setTextColor(resources.getColor(R.color.timeElapsed))
        timeElapsed!!.text = resources.getString(R.string.START_TIME)
        val timeElapParam =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        timeElapParam.setMargins(0, resources.getDimension(R.dimen.timeAndMemTopMargin).toInt(), 0, 0)
        timeElapParam.weight = 0.3f
        timeElapsed!!.layoutParams = timeElapParam
        cameraView!!.setTimeElapsedText(timeElapsed)
        timeElapsedParentLayout!!.layoutParams = parentLayoutParams
        timeElapsedParentLayout!!.addView(timeElapsed)
        settingsBar!!.addView(timeElapsedParentLayout)

        //Add memory consumed text
        memoryConsumed = TextView(activity)
        memoryConsumed!!.gravity = Gravity.CENTER_HORIZONTAL
        memoryConsumed!!.setTextColor(resources.getColor(R.color.memoryConsumed))
        memoryConsumed!!.typeface = Typeface.DEFAULT_BOLD
        memoryConsumed!!.setBackgroundColor(resources.getColor(R.color.transparentBar))
        memoryConsumed!!.text = resources.getString(R.string.START_MEMORY)
        val memConsumed =
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        memConsumed.setMargins(0, resources.getDimension(R.dimen.timeAndMemTopMargin).toInt(), 0, 0)
        memConsumed.weight = 0.3f
        memoryConsumed!!.layoutParams = memConsumed
        memoryConsumedParentLayout!!.layoutParams = parentLayoutParams
        memoryConsumedParentLayout!!.addView(memoryConsumed)
        settingsBar!!.addView(memoryConsumedParentLayout)
        cameraView!!.setMemoryConsumedText(memoryConsumed)
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(Constants.SHOW_MEMORY_CONSUMED_MSG, false)) {
            memoryConsumed!!.visibility = View.VISIBLE
        } else {
            memoryConsumed!!.visibility = View.INVISIBLE
        }
    }

    var isFlashOn = false
    private fun setFlash() {
        if (!isFlashOn) {
            if (VERBOSE) Log.d(TAG, "Flash on")
            if (cameraView!!.isFlashModeSupported(cameraView!!.cameraImplementation.flashModeTorch)) {
                isFlashOn = true
                flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_off))
                val feature = settingsMsgRoot!!.findViewById<View>(R.id.feature) as TextView
                feature.text =
                    resources.getString(R.string.flashSetting).uppercase(Locale.getDefault())
                val value = settingsMsgRoot!!.findViewById<View>(R.id.value) as TextView
                value.text = resources.getString(R.string.torchMode).uppercase(Locale.getDefault())
                val heading = settingsMsgRoot!!.findViewById<View>(R.id.heading) as ImageView
                heading.setImageDrawable(resources.getDrawable(R.drawable.torch))
                val settingsMsg =
                    Toast.makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)
                settingsMsg.setGravity(Gravity.CENTER, 0, 0)
                settingsMsg.view = settingsMsgRoot
                settingsMsg.show()
                Thread {
                    try {
                        Thread.sleep(1250)
                        settingsMsg.cancel()
                    } catch (ie: InterruptedException) {
                        ie.printStackTrace()
                    }
                }.start()
            } else {
                if (cameraView!!.cameraImplementation.flashModeTorch.equals(resources.getString(R.string.torchMode), ignoreCase = true)) {
                    Toast.makeText(applicationContext, resources.getString(R.string.flashModeNotSupported, resources.getString(R.string.torchMode)), Toast.LENGTH_SHORT).show()
                } else if (cameraView!!.cameraImplementation.flashModeTorch.equals(resources.getString(R.string.singleMode), ignoreCase = true)) {
                    Toast.makeText(applicationContext, resources.getString(R.string.flashModeNotSupported, resources.getString(R.string.singleMode)), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (VERBOSE) Log.d(TAG, "Flash off")
            isFlashOn = false
            flash!!.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_on))
            val feature = settingsMsgRoot!!.findViewById<View>(R.id.feature) as TextView
            feature.text = resources.getString(R.string.flashSetting).uppercase(Locale.getDefault())
            val value = settingsMsgRoot!!.findViewById<View>(R.id.value) as TextView
            value.text = resources.getString(R.string.flashOffMode).uppercase(Locale.getDefault())
            val heading = settingsMsgRoot!!.findViewById<View>(R.id.heading) as ImageView
            heading.setImageDrawable(resources.getDrawable(R.drawable.camera_flash_off))
            val settingsMsg =
                Toast.makeText(requireActivity().applicationContext, "", Toast.LENGTH_SHORT)
            settingsMsg.setGravity(Gravity.CENTER, 0, 0)
            settingsMsg.view = settingsMsgRoot
            settingsMsg.show()
            Thread {
                try {
                    Thread.sleep(1250)
                    settingsMsg.cancel()
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }
            }.start()
        }
        cameraView!!.flashOnOff(isFlashOn)
    }

    fun askForPermissionAgain() {
        if (VERBOSE) Log.d(TAG, "permissionInterface = $permissionInterface")
        permissionInterface!!.askPermission()
    }

    fun deleteLatestBadFile() {
        if (VERBOSE) Log.d(TAG, "Deleting bad file.. " + cameraView!!.mediaPath)
        val badFile = File(cameraView!!.mediaPath)
        if (badFile.exists()) {
            if (badFile.delete()) {
                if (VERBOSE) Log.d(TAG, "Bad file removed")
            }
        }
    }

    fun createAndShowThumbnail(mediaPath: String) { //Storing in public folder. This will ensure that the files are visible in other apps as well.
        //Use this for sharing files between apps
        val video = File(mediaPath)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(mediaPath)
        var firstFrame = mediaMetadataRetriever.getFrameAtTime(Constants.FIRST_SEC_MICRO)
        if (firstFrame == null) {
            if (video != null && video.delete()) {
                if (VERBOSE) Log.d(TAG, "Removed file = $mediaPath")
            }
            return
        }
        if (VERBOSE) Log.d(TAG, "width = " + firstFrame.width + " , height = " + firstFrame.height)
        var isDetached = false
        try {
            firstFrame =
                Bitmap.createScaledBitmap(firstFrame, resources.getDimension(R.dimen.thumbnailWidth).toInt(), resources.getDimension(R.dimen.thumbnailHeight).toInt(), false)
        } catch (illegal: IllegalStateException) {
            if (VERBOSE) Log.d(TAG, "video fragment is already detached. ")
            isDetached = true
        }

        addMediaToDB()
        if (!isDetached) {
            updateMicroThumbnailAsPerPlayer()
            microThumbnail!!.visibility = View.VISIBLE
            thumbnail!!.setImageBitmap(firstFrame)
            thumbnail!!.isClickable = true
            thumbnail!!.setOnClickListener { openMedia() }
        }
    }

    private fun updateMicroThumbnailAsPerPlayer() {
        if (isUseFCPlayer) {
            microThumbnail!!.setImageDrawable(resources.getDrawable(R.drawable.ic_play_circle_outline))
        } else {
            microThumbnail!!.setImageDrawable(resources.getDrawable(R.drawable.ic_external_play_circle_outline))
        }
    }

    fun isImage(path: String): Boolean {
        return if (path.endsWith(resources.getString(R.string.IMG_EXT)) || path.endsWith(resources.getString(R.string.ANOTHER_IMG_EXT))) {
            true
        } else false
    }

    fun addMediaToDB() {
        Log.d(TAG, "mediapath ===> " + cameraView!!.mediaPath.toString())
        val wordViewModel: MarkerViewModel by requireActivity().viewModels {
            WordViewModelFactory((requireActivity().application as ControlVisbilityPreference).repository)
        }
        DialogUtils.dialogChildNameOrRewardMsg(requireActivity(), getString(R.string.app_name), "Enter Video Name", AppConstants.DialogCodes.DIALOG_CLAIM_REWARD, object :
            DialogClickInterface {
            override fun onClick(code: Int, msg: String) {
                AppConstants.DialogCodes.apply {
                    when (code) {
                        DIALOG_CLAIM_REWARD -> {
                            wordViewModel.insert(
                                MarkerModel(
                                    latitude = currentLatitude,
                                    longitude = currentLongitude,
                                    videopath = cameraView!!.mediaPath.toString(),
                                    videoname = msg,
                                    isserver = false
                                )
                            )
                            showRecordSaved()
                            setCameraClose()
                            requireActivity().finishAffinity()
                        }
                    }
                }
            }

        })

    }


    fun deleteFileAndRefreshThumbnail() {
        val badFile = File(filePath)
        badFile.delete()
        if (VERBOSE) Log.d(TAG, "Bad file removed....$filePath")
        latestFileIfExists
    }

    var filePath = "" //Possible bad file in SD Card. Remove it.

    //Possible bad file in SD Card. Remove it.
    //If video cannot be played for whatever reason
    val latestFileIfExists: Unit
        get() {
            val medias = MediaUtil.getMediaList(requireActivity().applicationContext, false)
            if (medias != null && medias.size > 0) {
                if (VERBOSE) Log.d(TAG, "Latest file is = " + medias[0].path)
                filePath = medias[0].path
                if (!isImage(filePath)) {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    try {
                        mediaMetadataRetriever.setDataSource(filePath)
                    } catch (runtime: RuntimeException) {
                        if (VERBOSE) Log.d(TAG, "RuntimeException " + runtime.message)
                        if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) { //Possible bad file in SD Card. Remove it.
                            deleteFileAndRefreshThumbnail()
                            return
                        }
                    }
                    var vid = mediaMetadataRetriever.getFrameAtTime(Constants.FIRST_SEC_MICRO)
                    if (VERBOSE) Log.d(TAG, "Vid = $vid") //If video cannot be played for whatever reason
                    if (vid != null) {
                        vid =
                            Bitmap.createScaledBitmap(vid, resources.getDimension(R.dimen.thumbnailWidth).toInt(), resources.getDimension(R.dimen.thumbnailHeight).toInt(), false)
                        thumbnail!!.setImageBitmap(vid)
                        updateMicroThumbnailAsPerPlayer()
                        microThumbnail!!.visibility = View.VISIBLE
                        if (VERBOSE) Log.d(TAG, "set as image bitmap")
                        thumbnail!!.isClickable = true
                        thumbnail!!.setOnClickListener { openMedia() }
                    } else { //Possible bad file in SD Card. Remove it.
                        deleteFileAndRefreshThumbnail()
                        return
                    }
                } else {
                    try {
                        exifInterface = ExifInterface(filePath)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (VERBOSE) Log.d(TAG, "TAG_ORIENTATION = " + exifInterface!!.getAttribute(ExifInterface.TAG_ORIENTATION))
                    var pic = BitmapFactory.decodeFile(filePath)
                    pic =
                        Bitmap.createScaledBitmap(pic!!, resources.getDimension(R.dimen.thumbnailWidth).toInt(), resources.getDimension(R.dimen.thumbnailHeight).toInt(), false)
                    thumbnail!!.setImageBitmap(pic)
                    microThumbnail!!.visibility = View.INVISIBLE
                    thumbnail!!.isClickable = true
                    thumbnail!!.setOnClickListener { openMedia() }
                }
            } else {
                microThumbnail!!.visibility = View.INVISIBLE
                setPlaceholderThumbnail()
            }
        }

    fun setPlaceholderThumbnail() {
        thumbnail!!.setImageDrawable(resources.getDrawable(R.drawable.placeholder))
        thumbnail!!.isClickable = false
    }

    private fun openMedia() {
        setCameraClose()
        val mediaIntent = Intent(requireActivity().applicationContext, MediaActivity::class.java)
        val mediaLocEdit = sharedPreferences!!.edit()
        val mediaLocValue =
            if (sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) resources.getString(R.string.phoneLocation) else resources.getString(R.string.sdcardLocation)
        mediaLocEdit.putString(Constants.MEDIA_LOCATION_VIEW_SELECT, mediaLocValue)
        mediaLocEdit.commit()
        if (controlVisbilityPreference == null) {
            controlVisbilityPreference = applicationContext as ControlVisbilityPreference?
        }
        controlVisbilityPreference!!.isFromGallery = false
        startActivity(mediaIntent)
    }

    private fun setCameraClose() { //Set this if you want to continue when the launcher activity resumes.
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("startCamera", false)
        editor.commit()
    }

    private fun setCameraQuit() { //Set this if you want to quit the app when launcher activity resumes.
        val editor = sharedPreferences!!.edit()
        editor.putBoolean("startCamera", true)
        editor.commit()
    }

    override fun onDetach() {
        super.onDetach()
        if (VERBOSE) Log.d(TAG, "Detached")
    }

    override fun onResume() {
        super.onResume()
        if (VERBOSE) Log.d(TAG, "onResume")
        if (cameraView != null) {
            cameraView!!.visibility = View.VISIBLE //            autoStart()
        }
        orientationEventListener!!.enable()
        mediaFilters!!.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
        mediaFilters!!.addDataScheme("file")
        if (activity != null) {
            requireActivity().registerReceiver(sdCardEventReceiver, mediaFilters)
        }
        sdCardUnavailWarned = false
        checkForSDCard()
        startLocationUpdates()
    }

    override fun onDestroy() {
        if (VERBOSE) Log.d(TAG, "Fragment destroy...app is being minimized")
        setCameraClose()
        super.onDestroy()
    }

    override fun onStop() {
        if (VERBOSE) Log.d(TAG, "Fragment stop...app is out of focus")
        super.onStop()
    }

    override fun onPause() {
        if (VERBOSE) Log.d(TAG, "Fragment pause....app is being quit")
        setCameraQuit()
        if (cameraView != null) {
            if (VERBOSE) Log.d(TAG, "cameraview onpause visibility= " + cameraView!!.windowVisibility)
            if (cameraView!!.windowVisibility == View.VISIBLE) {
                cameraView!!.visibility = View.GONE
            }
        }
        orientationEventListener!!.disable()
        if (activity != null) {
            requireActivity().unregisterReceiver(sdCardEventReceiver)
        }
        locationManager?.removeUpdates(locationListener)
        super.onPause()
    }

    companion object {
        const val TAG = "VideoFragment"
        private var fragment: VideoFragment? = null
        fun newInstance(): VideoFragment? {
            Log.d(TAG, "NEW INSTANCE")
            if (fragment == null) {
                fragment = VideoFragment()
            }
            return fragment
        }
    }
}