package com.example.mapwidgetdemo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.mapwidgetdemo.databinding.ActivitySettingsMainBinding
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

class SettingsActivity : BaseActivity() {

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
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, true)
            } else {
                if (isGuest) {
                    val intent = Intent(this, LoginActivity::class.java)
                    resultLauncher.launch(intent)
                } else {
                    SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
                }
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
                "Welcome, " + SharedPreferenceUtils.preferenceGetString(AppConstants.SharedPreferenceKeys.NAME)

            binding.SwitchUpload.isChecked = !isGuest


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
            binding.texUserMode.text = "GUEST"
            binding.SwitchRemoveFromDevice.isEnabled = false
            binding.SwitchRemoveFromDevice.isClickable = false
            binding.SwitchRemoveFromDevice.isChecked = false
            binding.SwitchRemoveFromDevice.isFocusable = false
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
                binding.SwitchUpload.isChecked = true
                setUserRole()
            }
        }
}