package com.udacity.astroapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import com.udacity.astroapp.R
import com.udacity.astroapp.activities.MainActivity
import com.udacity.astroapp.models.Photo
import com.udacity.astroapp.repository.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Implementation of App Widget functionality. Displays the daily astronomy photo with proper video
 * handling.
 */
class AstroAppWidget : AppWidgetProvider() {

    companion object {
        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        intent.action?.let { action ->
            if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidget = ComponentName(context.packageName, AstroAppWidget::class.java.name)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget)

                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image)
                appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetIds,
                    R.id.widget_image_label
                )
                appWidgetManager.notifyAppWidgetViewDataChanged(
                    appWidgetIds,
                    R.id.widget_image_title
                )
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        appWidgetIds: IntArray
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.astro_app_widget)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)

        loadPhotoData(context, remoteViews, appWidgetManager, appWidgetId, appWidgetIds)
    }

    private fun loadPhotoData(
        context: Context,
        remoteViews: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        appWidgetIds: IntArray
    ) {
        widgetScope.launch {
            try {
                val photoRepository = GlobalContext.get().get<PhotoRepository>()
                val photos = photoRepository.getAllPhotos().first()

                if (photos.isEmpty()) {
                    showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
                    return@launch
                }

                val latestPhoto = photos.first()

                if (isVideoContent(latestPhoto)) {
                    showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
                } else {
                    loadPhotoImage(context, latestPhoto, remoteViews, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun isVideoContent(photo: Photo): Boolean {
        return photo.photoMediaType?.lowercase()?.contains("video") == true ||
            photo.photoUrl?.contains("youtube") == true ||
            photo.photoUrl?.contains("vimeo") == true
    }

    private suspend fun loadPhotoImage(
        context: Context,
        photo: Photo,
        remoteViews: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val photoUrl = photo.photoUrl
        if (photoUrl == null) {
            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
            return
        }

        try {
            val imageLoader = GlobalContext.get().get<ImageLoader>()

            val request =
                ImageRequest.Builder(context)
                    .data(photoUrl.toUri())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .build()

            val result = imageLoader.execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

            if (bitmap != null) {
                remoteViews.setImageViewBitmap(R.id.widget_image, bitmap)
                remoteViews.setTextViewText(R.id.widget_image_title, photo.photoTitle ?: "")
                remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.widget_image_title, View.VISIBLE)
                remoteViews.setViewVisibility(R.id.widget_image_label, View.VISIBLE)
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            } else {
                showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
            }
        } catch (e: Exception) {
            showFallbackContent(remoteViews, appWidgetManager, appWidgetId)
        }
    }

    private fun showFallbackContent(
        remoteViews: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE)
        remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE)
        remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE)
        remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }
}
