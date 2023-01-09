package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.databinding.ActivityLoginBinding
import com.example.mapwidgetdemo.databinding.ActivityRegisterBinding
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.DialogUtils
import kotlinx.coroutines.launch

class RegistrationActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
                                DialogUtils.hideProgressBar()
                                startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                                finish()
                            }
                        }
                    }
                    else -> {
                        val asString = event.asString(this@RegistrationActivity)
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                this@RegistrationActivity, asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    }


}