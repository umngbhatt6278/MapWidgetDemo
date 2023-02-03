package com.example.mapwidgetdemo.custom_camera

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.viewModels
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.custom_camera.PhotoFragment.*
import com.example.mapwidgetdemo.custom_camera.VideoFragment.*
import com.example.mapwidgetdemo.custom_camera.constants.Constants
import com.example.mapwidgetdemo.custom_camera.util.GLUtil
import com.example.mapwidgetdemo.custom_camera.view.PinchZoomGestureListener
import com.example.mapwidgetdemo.ui.activity.BaseActivity
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity : BaseActivity(), PermissionInterface, PhotoPermission, SwitchInterface,
    SwitchPhoto, LowestThresholdCheckForVideoInterface, LowestThresholdCheckForPictureInterface {
    var videoFragment: VideoFragment? = null
    var photoFragment: PhotoFragment? = null
    var warningMsgRoot: View? = null
    var warningMsg: Dialog? = null
    var okButton: Button? = null

    var sharedPreferences: SharedPreferences? = null
    var VERBOSE = false
    var settingsRootView: View? = null
    var settingsDialog: Dialog? = null
    var brightness: ImageView? = null
    var controlVisbilityPreference: ControlVisbilityPreference? = null
    var fromGallery = false
    var pinchZoomGestureListener: PinchZoomGestureListener? = null
    var scaleGestureDetector: ScaleGestureDetector? = null

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as ControlVisbilityPreference).repository)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector!!.onTouchEvent(event)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (VERBOSE) Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_camera)
        brightness = findViewById<View>(R.id.brightness) as ImageView
        controlVisbilityPreference =
            applicationContext as ControlVisbilityPreference //        getSupportActionBar().hide();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        if (savedInstanceState == null) { //Start with video fragment
            showVideoFragment()
            controlVisbilityPreference!!.brightnessLevel = Constants.NORMAL_BRIGHTNESS
            controlVisbilityPreference!!.brightnessProgress = 0.0f
        }
        Companion.layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        warningMsgRoot = Companion.layoutInflater!!.inflate(R.layout.warning_message, null)
        warningMsg = Dialog(this)
        settingsRootView = Companion.layoutInflater!!.inflate(R.layout.brightness_settings, null)
        settingsDialog = Dialog(this)
        sharedPreferences = getSharedPreferences(Constants.FC_SETTINGS, MODE_PRIVATE)
        val bundle = intent.extras
        if (bundle != null) {
            fromGallery = bundle.getBoolean("fromGallery")
        }
        setPinchZoomScaleListener(if (videoFragment != null) videoFragment else null, if (photoFragment != null) photoFragment else null)

    }


    private fun setPinchZoomScaleListener(videoFragment: VideoFragment?, photoFragment: PhotoFragment?) {
        if (pinchZoomGestureListener != null) {
            pinchZoomGestureListener = null
        }
        pinchZoomGestureListener = PinchZoomGestureListener(
            applicationContext, videoFragment, photoFragment
        )
        scaleGestureDetector = ScaleGestureDetector(applicationContext, pinchZoomGestureListener)
    }

    fun displaySDCardNotDetectMessage() {
        if (VERBOSE) Log.d(TAG, "displaySDCardNotDetectMessage") //The below variable is needed to check if there was SD Card removed in MediaActivity which caused the control
        // to come here.
        if (fromGallery) { //Show SD Card not detected, please insert sd card to try again.
            val warningTitle = warningMsgRoot!!.findViewById<View>(R.id.warningTitle) as TextView
            warningTitle.text = resources.getString(R.string.sdCardNotDetectTitle)
            val warningText = warningMsgRoot!!.findViewById<View>(R.id.warningText) as TextView
            warningText.text = resources.getString(R.string.sdCardNotDetectMessage)
            okButton = warningMsgRoot!!.findViewById<View>(R.id.okButton) as Button
            okButton!!.setOnClickListener {
                warningMsg!!.dismiss()
                videoFragment!!.latestFileIfExists
            }
            warningMsg!!.setContentView(warningMsgRoot!!)
            warningMsg!!.setCancelable(false)
            warningMsg!!.show()
            if (VERBOSE) Log.d(TAG, "MESSAGE SHOWN")
        }
    }

    fun doesSDCardExist(): String? {
        var sdcardpath = sharedPreferences!!.getString(Constants.SD_CARD_PATH, "")
        try {
            val filename =
                "/doesSDCardExist_" + System.currentTimeMillis().toString().substring(0, 5)
            sdcardpath += filename
            val sdCardFilePath = sdcardpath
            val createTestFile = FileOutputStream(sdcardpath)
            if (VERBOSE) Log.d(TAG, "Able to create file... SD Card exists")
            Thread {
                val testfile = File(sdCardFilePath)
                try {
                    createTestFile.close()
                    testfile.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: FileNotFoundException) {
            if (VERBOSE) Log.d(TAG, "Unable to create file... SD Card NOT exists..... " + e.message)
            return null
        }
        return sharedPreferences!!.getString(Constants.SD_CARD_PATH, "")
    }

    fun goToSettings(view: View?) {/* Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);*/
    }

    fun openBrightnessPopup(view: View?) {
        val header = settingsRootView!!.findViewById<View>(R.id.timerText) as TextView
        header.text = resources.getString(R.string.brightnessHeading)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        settingsDialog!!.setContentView(settingsRootView!!)
        settingsDialog!!.setCancelable(true)
        val lp = settingsDialog!!.window!!.attributes
        lp.dimAmount = 0.0f
        lp.width = (size.x * 0.8).toInt()
        val brightnessBar = settingsRootView!!.findViewById<View>(R.id.brightnessBar) as SeekBar
        brightnessBar.max = 10
        brightnessBar.progress = controlVisbilityPreference!!.brightnessLevel
        brightnessBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.progress = controlVisbilityPreference!!.brightnessLevel
            }
        })
        val increaseBrightness =
            settingsRootView!!.findViewById<View>(R.id.increaseBrightness) as Button
        increaseBrightness.setOnClickListener {
            if (isVideo) {
                if (GLUtil.colorVal < 0.25f) {
                    GLUtil.colorVal += 0.05f
                    brightnessBar.incrementProgressBy(1)
                    controlVisbilityPreference!!.brightnessLevel = brightnessBar.progress
                } else {
                    GLUtil.colorVal = 0.25f
                }
                controlVisbilityPreference!!.brightnessProgress = GLUtil.colorVal
            }
        }
        val decreaseBrightness = settingsRootView!!.findViewById<View>(R.id.setTimer) as Button
        decreaseBrightness.setOnClickListener {
            if (isVideo) {
                if (GLUtil.colorVal > -0.25f) {
                    GLUtil.colorVal -= 0.05f
                    brightnessBar.incrementProgressBy(-1)
                    controlVisbilityPreference!!.brightnessLevel = brightnessBar.progress
                } else {
                    GLUtil.colorVal = -0.25f
                }
                controlVisbilityPreference!!.brightnessProgress = GLUtil.colorVal
            }
        }
        settingsDialog!!.window!!.setBackgroundDrawableResource(R.color.backColorSettingPopup)
        settingsDialog!!.show()
    }

    private val isVideo: Boolean
        private get() = videoFragment != null

    override fun switchToPhoto() {
        showPhotoFragment()
    }

    override fun switchToVideo() {
        showVideoFragment()
    }

    fun showVideoFragment() {
        if (videoFragment == null) {
            if (VERBOSE) Log.d(TAG, "creating videofragment")
            videoFragment = VideoFragment.newInstance(intent.extras)
            videoFragment!!.applicationContext = applicationContext
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.cameraPreview, videoFragment!!).commit()
        if (VERBOSE) Log.d(TAG, "brightnessLevel SET to = " + controlVisbilityPreference!!.brightnessLevel)
        brightness!!.visibility = View.VISIBLE
        setPinchZoomScaleListener(videoFragment, null)
    }

    fun showPhotoFragment() {
        brightness!!.visibility = View.GONE
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (photoFragment == null) {
            if (VERBOSE) Log.d(TAG, "creating photofragment")
            photoFragment = PhotoFragment.newInstance()
            photoFragment!!.applicationContext = applicationContext
        }
        setPinchZoomScaleListener(null, photoFragment)
        fragmentTransaction.replace(R.id.cameraPreview, photoFragment!!).commit()
        if (VERBOSE) Log.d(TAG, "photofragment added")
        val settingsEditor = sharedPreferences!!.edit()
        if (!sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) { //Check if SD Card exists
            if (doesSDCardExist() == null) {
                settingsEditor.putBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)
                settingsEditor.commit()
                val warningTitle =
                    warningMsgRoot!!.findViewById<View>(R.id.warningTitle) as TextView
                warningTitle.text = resources.getString(R.string.sdCardRemovedTitle)
                val warningText = warningMsgRoot!!.findViewById<View>(R.id.warningText) as TextView
                warningText.text = resources.getString(R.string.sdCardNotPresentForRecord)
                okButton = warningMsgRoot!!.findViewById<View>(R.id.okButton) as Button
                okButton!!.setOnClickListener {
                    warningMsg!!.dismiss()
                    photoFragment!!.getLatestFileIfExists()
                }
                warningMsg!!.setContentView(warningMsgRoot!!)
                warningMsg!!.setCancelable(false)
                warningMsg!!.show()
            }
        } else if (!checkIfPhoneMemoryIsBelowLowThreshold() && !sharedPreferences!!.getBoolean(Constants.PHONE_MEMORY_DISABLE, true)) {
            val editor = sharedPreferences!!.edit()
            val memoryThreshold =
                sharedPreferences!!.getString(Constants.PHONE_MEMORY_LIMIT, "")!!.toInt()
            val memoryMetric = sharedPreferences!!.getString(Constants.PHONE_MEMORY_METRIC, "")
            val storageStat = StatFs(Environment.getDataDirectory().path)
            var memoryValue: Long = 0
            var metric = ""
            when (memoryMetric) {
                Constants.METRIC_MB -> {
                    memoryValue = memoryThreshold * Constants.MEGA_BYTE.toLong()
                    metric = Constants.METRIC_MB
                }
                Constants.METRIC_GB -> {
                    memoryValue = memoryThreshold * Constants.GIGA_BYTE.toLong()
                    metric = Constants.METRIC_GB
                }
            }
            if (VERBOSE) Log.d(TAG, "memory value = $memoryValue")
            if (VERBOSE) Log.d(TAG, "Avail mem = " + storageStat.availableBytes)
            if (storageStat.availableBytes < memoryValue) {
                val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val thresholdExceededRoot =
                    layoutInflater.inflate(R.layout.threshold_exceeded, null)
                val thresholdDialog = Dialog(this)
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
                }
                val memThreshold = StringBuilder(memoryThreshold.toString() + "")
                memThreshold.append(" ")
                memThreshold.append(metric)
                if (VERBOSE) Log.d(TAG, "memory threshold for display = $memThreshold")
                memoryLimitMsg.text =
                    resources.getString(R.string.thresholdLimitExceededMsg, memThreshold.toString())
                thresholdDialog.setContentView(thresholdExceededRoot)
                thresholdDialog.setCancelable(false)
                thresholdDialog.show()
            }
        }
    }

    fun checkIfPhoneMemoryIsBelowLowThreshold(): Boolean {
        return if (sharedPreferences!!.getBoolean(Constants.SAVE_MEDIA_PHONE_MEM, true)) {
            val storageStat = StatFs(Environment.getDataDirectory().path)
            val lowestThreshold = resources.getInteger(R.integer.minimumMemoryWarning)
            val lowestMemory = lowestThreshold * Constants.MEGA_BYTE.toLong()
            if (VERBOSE) Log.d(TAG, "lowestMemory = $lowestMemory")
            if (VERBOSE) Log.d(TAG, "avail mem = " + storageStat.availableBytes)
            if (storageStat.availableBytes < lowestMemory) {
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun checkIfPhoneMemoryIsBelowLowestThresholdForPicture(): Boolean {
        return checkIfPhoneMemoryIsBelowLowThreshold()
    }

    override fun checkIfPhoneMemoryIsBelowLowestThresholdForVideo(): Boolean {
        return checkIfPhoneMemoryIsBelowLowThreshold()
    }

    override fun onStart() {
        super.onStart()
        if (VERBOSE) Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        if (VERBOSE) Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (VERBOSE) Log.d(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        if (VERBOSE) Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        if (VERBOSE) Log.d(TAG, "onPause")
        if (videoFragment != null) {
            videoFragment!!.zoomBar?.progress = 0
        } else if (photoFragment != null) {
            photoFragment!!.zoomBar.progress = 0
        }
        pinchZoomGestureListener!!.setProgress(0)
    }

    override fun askPermission() {
        askCameraPermission()
    }

    override fun askPhotoPermission() {
        askCameraPermission()
    }

    fun askCameraPermission() {
        if (VERBOSE) Log.d(TAG, "start permission act to get permissions")
        val permission = Intent(this, PermissionActivity::class.java)
        permission.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(permission)
        finish()
    }

    companion object {
        private const val TAG = "CameraActivity"
        var layoutInflater: LayoutInflater? = null
    }
}