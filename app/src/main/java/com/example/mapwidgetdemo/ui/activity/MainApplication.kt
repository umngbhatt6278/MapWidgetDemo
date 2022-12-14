package com.example.mapwidgetdemo.ui.activity

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.example.mapwidgetdemo.ui.activity.database.MarkerRepository
import com.example.mapwidgetdemo.ui.activity.database.db.MapDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

open class MainApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { MapDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { MarkerRepository(database.markerDao()) }

    override fun onCreate() {
        super.onCreate()
        setupStrictPolicy()
    }

    private fun setupStrictPolicy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .build()
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}