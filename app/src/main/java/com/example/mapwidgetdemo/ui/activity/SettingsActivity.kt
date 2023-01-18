package com.example.mapwidgetdemo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivitySettingsMainBinding
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.AppConstants.DialogCodes.Companion.DIALOG_SIGN_OUT
import com.example.mapwidgetdemo.utils.DialogClickInterface
import com.example.mapwidgetdemo.utils.DialogUtils
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

class SettingsActivity : BaseActivity(), DialogClickInterface {

    private lateinit var binding: ActivitySettingsMainBinding

    var isGuest: Boolean = true
    var isremoveFromDevice: Boolean = false
    var removeFromServerData: ArrayList<MarkerModel> = ArrayList()

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUserRole()

        binding.SwitchUpload.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (isGuest) {
                    DialogUtils.alertDialogSignOut(
                        this, getString(R.string.app_name), getString(R.string.upload_server_msg), AppConstants.DialogCodes.DIALOG_SIGN_OUT, this@SettingsActivity
                    )
                } else {
                    SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
                }
            } else {
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
            }


        }

        binding.SwitchRemoveFromDevice.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, isChecked)
                removedatafromDevice()
            } else {
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, isChecked)
            }

        }

        binding.texViewAll.setOnClickListener {
            val intent = Intent(this, MapPinActivity::class.java)
            startActivity(intent)
        }

        binding.texLogout.setOnClickListener {
            SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_GUEST, true)
            SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
            SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_REMOVE_FROM_DEVICE, false)
            SharedPreferenceUtils.preferencePutString(AppConstants.SharedPreferenceKeys.F_TOKEN, "")
            binding.SwitchUpload.isChecked = false

            wordViewModel.allWords.observe(this@SettingsActivity) { words -> // Update the cached copy of the words in the adapter.
                words.let {
                    val data = it
                    removeFromServerData = data as ArrayList<MarkerModel>
                    Log.d("logger", "when logout time ==> " + removeFromServerData.size)

                    SharedPreferenceUtils.saveArrayList(removeFromServerData, AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)
                }
            }


            setUserRole()
            binding.imgBack.performClick()
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
    }


    private fun removedatafromDevice() {
        if (removeFromServerData.size > 0) {
            for (i in removeFromServerData.indices) {
                if (removeFromServerData[i].isserver) {
                    wordViewModel.delete(
                        MarkerModel(
                            id = removeFromServerData[i].id,
                            latitude = removeFromServerData[i].latitude,
                            longitude = removeFromServerData[i].longitude,
                            videopath = removeFromServerData[i].videopath,
                            videoname = removeFromServerData[i].videoname,
                            isserver = removeFromServerData[i].isserver,
                        )
                    )
                }
            }
        }
    }

    fun setUserRole() {
        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_GUEST)) {
            isGuest =
                SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_GUEST, true)
        }
        if (!isGuest) {
            binding.texUserMode.text =
                "Welcome Back, " + SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.NAME)

            binding.SwitchUpload.isChecked = !isGuest
            binding.texLogout.isVisible = true

            if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_REMOVE_FROM_DEVICE)) {
                isremoveFromDevice =
                    SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_REMOVE_FROM_DEVICE, false)
            }


            binding.SwitchRemoveFromDevice.isChecked =
                SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_REMOVE_FROM_DEVICE, false)


            if (binding.SwitchRemoveFromDevice.isChecked) {
                removedatafromDevice()
            }

            if (binding.SwitchUpload.isChecked) {
                binding.SwitchRemoveFromDevice.isEnabled = true
                binding.SwitchRemoveFromDevice.isClickable = true
                binding.SwitchRemoveFromDevice.isFocusable = true
            } else {
                binding.SwitchRemoveFromDevice.isEnabled = false
                binding.SwitchRemoveFromDevice.isClickable = false
                binding.SwitchRemoveFromDevice.isFocusable = false
            }
        } else {
            binding.texUserMode.text = "Welcome, GUEST"
            binding.SwitchRemoveFromDevice.isEnabled = false
            binding.SwitchRemoveFromDevice.isClickable = false
            binding.SwitchRemoveFromDevice.isChecked = false
            binding.SwitchRemoveFromDevice.isFocusable = false
            binding.texLogout.isVisible = false
        }




        wordViewModel.allWords.observe(this@SettingsActivity) { words -> // Update the cached copy of the words in the adapter.
            words.let {
                val data = it
                removeFromServerData = data as ArrayList<MarkerModel>
                Log.d("logger", "In Setting Activity ==> " + removeFromServerData.size)
            }
        }
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, true)
                binding.SwitchUpload.isChecked = true
                setUserRole()
            }
        }

    override fun onClick(code: Int, msg: String) {
        AppConstants.DialogCodes.apply {
            when (code) {
                DIALOG_SIGN_OUT -> {
                    if (msg == "") {
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        resultLauncher.launch(intent)
                    } else {
                        binding.SwitchUpload.isChecked = false
                        SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
                    }
                }
            }
        }
    }
}