package com.example.mapwidgetdemo.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivityMapBinding
import com.example.mapwidgetdemo.databinding.ActivityVideoBinding

class MapPinActivity : BaseActivity() {

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {


    }
}