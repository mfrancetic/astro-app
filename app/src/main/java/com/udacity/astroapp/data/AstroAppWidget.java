package com.udacity.astroapp.data;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.fragments.PhotoFragment;

/**
 * Implementation of App Widget functionality.
 */
public class AstroAppWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager
            appWidgetManager, int appWidgetId) {

        /* Create a new RemoteViews variable with the astro_app_widget.xml layout */
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.astro_app_widget);

        /* Create a new intent that launches the MainActivity.class, as well as a pending Intent*/
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (PhotoFragment.photo != null && PhotoFragment.photoUrl != null && PhotoFragment.videoUri == null) {
                /* If the photo exists, load it into the widget_image remote view */
                Uri photoUri = Uri.parse(PhotoFragment.photoUrl);
                Picasso picasso = new Picasso.Builder(context).build();
                if (photoUri != null) {
                    picasso.load(photoUri)
                            .into(remoteViews, R.id.widget_image, new int[]{appWidgetId});

                    /* Set the text of the widget_image_title TextView to the photoTitle and make all
                     * views visible */
                    if (PhotoFragment.photoTitle != null) {
                        remoteViews.setTextViewText(R.id.widget_image_title, PhotoFragment.photoTitle);
                    }
                    remoteViews.setViewVisibility(R.id.widget_image_title, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.widget_image_label, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE);
                } else {
                    remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE);
                    remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE);
                    remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher);
                }
            }  else {
            /* In case there is no photo, hide the image label and title, and show only the
             * ic_launcher logo */
            remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE);
            remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE);
            remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE);
            remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher);
        }
        /* Set an OnClickPendingIntent to the widget */
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        /* In case there are multiple widgets active, update all of them */
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        /* If there is an action, update the app widget and notify it that the data has changed */
        if (intent.getAction() != null) {
            if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName appWidget = new ComponentName(context.getPackageName(), AstroAppWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_label);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_title);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout);
            }
        }
    }
}