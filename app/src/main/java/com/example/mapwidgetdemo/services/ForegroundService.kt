package com.example.mapwidgetdemo.services

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.ui.activity.HomeTabActivity


class ForegroundService : Service() {

    private val mBinder: IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val input: String = intent.getStringExtra("inputExtra")!!
        createNotificationChannel()
        val notificationIntent = Intent(this, HomeTabActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, FLAG_IMMUTABLE
        )
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_pin)
                .setContentIntent(pendingIntent).build()
        startForeground(1, notification) //do heavy work on a background thread
        //stopSelf();
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        val service: ForegroundService
            get() = this@ForegroundService
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
    }
}