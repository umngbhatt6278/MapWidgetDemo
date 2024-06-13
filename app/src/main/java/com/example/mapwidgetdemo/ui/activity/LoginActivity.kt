package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.databinding.ActivityLoginBinding
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.demoOne
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.EMAIL
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.F_TOKEN
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.IS_GUEST
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.NAME
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()



demoOne(name = "jsjhfkjsd")
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
                        val loginresponse = event.data as LoginResponse
                        SharedPreferenceUtils.preferencePutString(F_TOKEN, loginresponse.data.token)
                        SharedPreferenceUtils.preferencePutString(NAME, loginresponse.data.name)
                        SharedPreferenceUtils.preferencePutString(EMAIL, loginresponse.data.email)
                        SharedPreferenceUtils.preferencePutBoolean(IS_GUEST, false)
                        setResult(RESULT_OK)
                        finish()
                    }
                    else -> {
                        val asString = event.asString(this@LoginActivity)
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                this@LoginActivity, asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }



        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.txtForgotPasword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.imgBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }


}