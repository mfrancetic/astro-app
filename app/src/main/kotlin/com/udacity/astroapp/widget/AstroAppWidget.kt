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
import androidx.lifecycle.Observer
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import com.udacity.astroapp.R
import com.udacity.astroapp.activities.MainActivity
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.repository.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Implementation of App Widget functionality.
 * Displays the daily astronomy photo with proper video handling.
 */
//class AstroAppWidget : AppWidgetProvider() {
//
//    companion object {
//        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
//    }
//
//    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
//        // Update all widget instances
//        for (appWidgetId in appWidgetIds) {
//            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds)
//        }
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        super.onReceive(context, intent)
//
//        // Handle widget update actions
//        intent.action?.let { action ->
//            if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
//                val appWidgetManager = AppWidgetManager.getInstance(context)
//                val appWidget = ComponentName(context.packageName, AstroAppWidget::class.java.name)
//                val appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget)
//
//                // Notify that widget data has changed
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image)
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_label)
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_title)
//                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
//            }
//        }
//    }
//
//    private fun updateAppWidget(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        appWidgetIds: IntArray
//    ) {
//        // Create RemoteViews with the widget layout
//        val remoteViews = RemoteViews(context.packageName, R.layout.astro_app_widget)
//
//        // Create intent to launch MainActivity when widget is clicked
//        val intent = Intent(context, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Set click listener for the entire widget
//        remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
//
//        // Load the latest photo data
//        loadPhotoData(context, remoteViews, appWidgetManager, appWidgetId, appWidgetIds)
//    }
//
//    private fun loadPhotoData(
//        context: Context,
//        remoteViews: RemoteViews,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        appWidgetIds: IntArray
//    ) {
//        widgetScope.launch {
//            try {
//                // Get dependencies from Koin
//                val photoRepository = GlobalContext.get().get<PhotoRepository>()
//
//                // Observe the latest photos from repository
//                val photosObserver = object : Observer<List<Photo>> {
//                    override fun onChanged(photos: List<Photo>?) {
//                        if (photos.isNullOrEmpty()) {
//                            // No photos available - show fallback
//                            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//                            return
//                        }
//
//                        // Get the most recent photo (first in list, assuming sorted by date desc)
//                        val latestPhoto = photos.first()
//
//                        // Check if it's a video content
//                        if (isVideoContent(latestPhoto)) {
//                            // Show fallback for video content
//                            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//                        } else {
//                            // Load and display the photo
//                            loadPhotoImage(context, latestPhoto, remoteViews, appWidgetManager, appWidgetId, appWidgetIds)
//                        }
//                    }
//                }
//
//                // Start observing photos
//                photoRepository.loadAllPhotos().observeForever(photosObserver)
//
//            } catch (e: Exception) {
//                // Error occurred - show fallback
//                showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//            }
//        }
//    }
//
//    private fun isVideoContent(photo: Photo): Boolean {
//        // Check if the media type indicates video content
//        return photo.mediaType?.lowercase()?.contains("video") == true ||
//               photo.url?.contains("youtube") == true ||
//               photo.url?.contains("vimeo") == true
//    }
//
//    private fun loadPhotoImage(
//        context: Context,
//        photo: Photo,
//        remoteViews: RemoteViews,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        appWidgetIds: IntArray
//    ) {
//        photo.url?.let { photoUrl ->
//            try {
//                // Get ImageLoader from Koin
//                val imageLoader = GlobalContext.get().get<ImageLoader>()
//
//                val request = ImageRequest.Builder(context)
//                    .data(Uri.parse(photoUrl))
//                    .placeholder(R.mipmap.ic_launcher)
//                    .error(R.mipmap.ic_launcher)
//                    .target(object : Target {
//                        override fun onSuccess(result: Bitmap) {
//                            // Successfully loaded image
//                            remoteViews.setImageViewBitmap(R.id.widget_image, result)
//                            remoteViews.setTextViewText(R.id.widget_image_title, photo.title ?: "")
//
//                            // Show all views
//                            remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
//                            remoteViews.setViewVisibility(R.id.widget_image_title, View.VISIBLE)
//                            remoteViews.setViewVisibility(R.id.widget_image_label, View.VISIBLE)
//
//                            // Update the widget
//                            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
//                        }
//
//                        override fun onError(error: Throwable?) {
//                            // Failed to load image - show fallback
//                            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//                        }
//                    })
//                    .build()
//
//                imageLoader.enqueue(request)
//            } catch (e: Exception) {
//                // Failed to get ImageLoader or create request - show fallback
//                showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//            }
//        } ?: run {
//            // No URL available - show fallback
//            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
//        }
//    }
//
//    private fun showFallbackContent(
//        remoteViews: RemoteViews,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int
//    ) {
//        // Hide title and label, show only app icon
//        remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE)
//        remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
//        remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
//        remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher)
//
//        // Update the widget
//        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
//    }
//}