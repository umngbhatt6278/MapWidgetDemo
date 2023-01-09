package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.IS_GUEST)) {
            isGuest =
                SharedPreferenceUtils.preferenceGetBoolean(AppConstants.SharedPreferenceKeys.IS_GUEST, true)
        }

        if (!isGuest) {
            binding.texUserMode.text = "Login"
        } else {
            binding.texUserMode.text = "GUEST"
        }


        binding.SwitchUpload.setOnClickListener {
            if (isGuest) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

    }
}