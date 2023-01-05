package com.example.mapwidgetdemo.ui.activity

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.example.mapwidgetdemo.di.appModule
import com.example.mapwidgetdemo.ui.activity.database.MarkerRepository
import com.example.mapwidgetdemo.ui.activity.database.db.MapDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

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

    val database by lazy { MapDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MarkerRepository(database.markerDao()) }

    override fun onCreate() {
        super.onCreate()
        setupStrictPolicy()
        startKoin {
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }

    private fun setupStrictPolicy() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().penaltyLog().detectDiskReads().detectDiskWrites().detectNetwork().build()
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}