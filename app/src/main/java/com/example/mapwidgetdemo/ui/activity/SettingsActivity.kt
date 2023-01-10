package com.example.mapwidgetdemo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.mapwidgetdemo.databinding.ActivitySettingsMainBinding
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsMainBinding

    var isGuest: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setUserRole()

        binding.SwitchUpload.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isGuest) {
                val intent = Intent(this, LoginActivity::class.java)
                resultLauncher.launch(intent)
            } else {
                SharedPreferenceUtils.preferencePutBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, isChecked)
            }
        }

        binding.texViewAll.setOnClickListener {
            val intent = Intent(this, MapPinActivity::class.java)
            startActivity(intent)
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
            binding.SwitchUpload.isChecked =
                SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_UPLOAD_SERVER, false)
            if (binding.SwitchUpload.isChecked) {
                binding.SwitchRemoveFromDevice.isEnabled = true
                binding.SwitchRemoveFromDevice.isClickable = true
                binding.SwitchRemoveFromDevice.isChecked = true
                binding.SwitchRemoveFromDevice.isFocusable = true
            }else{
                binding.SwitchRemoveFromDevice.isEnabled = false
                binding.SwitchRemoveFromDevice.isClickable = false
                binding.SwitchRemoveFromDevice.isChecked = false
                binding.SwitchRemoveFromDevice.isFocusable = false
            }
        } else {
            binding.texUserMode.text = "GUEST"
            binding.SwitchRemoveFromDevice.isEnabled = false
            binding.SwitchRemoveFromDevice.isClickable = false
            binding.SwitchRemoveFromDevice.isChecked = false
            binding.SwitchRemoveFromDevice.isFocusable = false
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