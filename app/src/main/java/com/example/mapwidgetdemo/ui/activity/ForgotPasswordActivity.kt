package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.databinding.ActivityForgotPasswordBinding
import com.example.mapwidgetdemo.databinding.ActivityLoginBinding
import com.example.mapwidgetdemo.response.CommonErrorResponse
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.EMAIL
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.F_TOKEN
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.IS_GUEST
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.NAME
import kotlinx.coroutines.launch

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.loginViewModel = loginViewModel


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
                        val loginresponse = event.data as CommonErrorResponse
                        Toast.makeText(this@ForgotPasswordActivity, loginresponse.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        val asString = event.asString(this@ForgotPasswordActivity)
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                this@ForgotPasswordActivity, asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        binding.imgBack.setOnClickListener {
            finish()
        }
    }


}