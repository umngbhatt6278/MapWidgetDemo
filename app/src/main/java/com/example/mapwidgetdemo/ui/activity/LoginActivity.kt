package com.example.mapwidgetdemo.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.databinding.ActivityLoginBinding
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.DialogUtils
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

                            }
                        }
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

    }


}