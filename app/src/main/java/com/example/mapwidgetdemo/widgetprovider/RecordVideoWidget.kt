package com.example.mapwidgetdemo.widgetprovider

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.ui.activity.CameraActivity
import com.example.mapwidgetdemo.ui.activity.MapActivity

/**
 * Implementation of App Widget functionality.
 */
class RecordVideoWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val intent = Intent(context, CameraActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    intent.putExtra("IS_FROM_WIDGET", true)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.record_video_widget)
    views.setOnClickPendingIntent(R.id.appwidget_video, pendingIntent)
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}