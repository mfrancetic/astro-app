package com.udacity.astroapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.transition.Transition
import com.udacity.astroapp.R
import com.udacity.astroapp.data.AstroDao
import com.udacity.astroapp.data.AppDatabase
import com.udacity.astroapp.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality for AstroApp.
 * Shows the current Astronomy Picture of the Day (APOD) in a widget.
 */
class AstroAppWidget : AppWidgetProvider() {

    companion object {
        private const val LOG_TAG = "AstroAppWidget"

        fun updateAppWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds)
            }
        }

        private fun updateAppWidget(
            context: Context, 
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int, 
            appWidgetIds: IntArray
        ) {
            // Create RemoteViews with the widget layout
            val remoteViews = RemoteViews(context.packageName, R.layout.astro_app_widget)
            
            // Create PendingIntent to launch MainActivity when widget is clicked
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Set the click intent for the entire widget
            remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
            
            // Load photo data from database in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getInstance(context)
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val photo = database.astroDao().getPhotoByDate(today)
                    
                    withContext(Dispatchers.Main) {
                        if (photo?.photoUrl != null && photo.photoMediaType == "image") {
                            // Load image with Glide
                            val photoUri = Uri.parse(photo.photoUrl)
                            val appWidgetTarget = object : AppWidgetTarget(
                                context, 
                                R.id.widget_image, 
                                remoteViews, 
                                appWidgetIds
                            ) {
                                override fun onResourceReady(
                                    resource: Bitmap, 
                                    transition: Transition<in Bitmap>?
                                ) {
                                    super.onResourceReady(resource, transition)
                                }
                                
                                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    // Show default launcher icon if image fails to load
                                    remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher)
                                    remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
                                    remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE)
                                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                                }
                            }
                            
                            Glide.with(context.applicationContext)
                                .asBitmap()
                                .load(photoUri)
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .transition(BitmapTransitionOptions.withCrossFade())
                                .centerCrop()
                                .into(appWidgetTarget)
                            
                            // Set photo title if available
                            photo.photoTitle?.let { title ->
                                remoteViews.setTextViewText(R.id.widget_image_title, title)
                                remoteViews.setViewVisibility(R.id.widget_image_title, View.VISIBLE)
                            } ?: run {
                                remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
                            }
                            
                            // Show image label
                            remoteViews.setViewVisibility(R.id.widget_image_label, View.VISIBLE)
                            remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
                            
                        } else {
                            // No photo or video content - show default launcher icon
                            remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher)
                            remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
                            remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE)
                            remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
                        }
                        
                        // Update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                    }
                } catch (e: Exception) {
                    // On error, show default launcher icon
                    withContext(Dispatchers.Main) {
                        remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher)
                        remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
                        remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE)
                        remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
                        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                    }
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Update all widgets
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle widget update actions
        intent.action?.let { action ->
            if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidget = ComponentName(context.packageName, AstroAppWidget::class.java.name)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget)
                
                // Notify that widget data has changed
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_label)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_title)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
                
                // Update widgets
                updateAppWidgets(context, appWidgetManager, appWidgetIds)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
        super.onDisabled(context)
    }
}