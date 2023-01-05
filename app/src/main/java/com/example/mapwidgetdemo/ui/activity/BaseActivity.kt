package com.example.mapwidgetdemo.ui.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mapwidgetdemo.utils.ConnectionLiveData
import com.example.mapwidgetdemo.utils.isConnectedToInternet
import com.example.mapwidgetdemo.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseActivity: AppCompatActivity(){

    private var connectionLiveData: ConnectionLiveData? = null
    val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        loginViewModel.isNetworkAvailable.value = isConnectedToInternet()
        connectionLiveData = ConnectionLiveData(this)
    }

    fun checkPermission(): Boolean {
        val location = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val write = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val read = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val camera = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.CAMERA
        )
        val audio = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.RECORD_AUDIO
        )
        return location == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
                && camera == PackageManager.PERMISSION_GRANTED && audio == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
