package com.udacity.astroapp.data;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.fragments.PhotoFragment;
import com.udacity.astroapp.utils.QueryUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

public class AstroAppWidget extends AppWidgetProvider {

    private static final String photoTitleKey = "photo";

    private static final String photoUrlKey = "photoUrl";

    private static Bitmap bitmap;


    private static void updateAppWidget(Context context, AppWidgetManager
            appWidgetManager, int appWidgetId) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.astro_app_widget);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        if (PhotoFragment.photo != null) {
            if (PhotoFragment.photoTitle != null && PhotoFragment.photoUrl != null) {

//            String photoTitle = sharedPreferences.getString(photoTitleKey);
//            String photoUrl = sharedPreferences.getString(photoUrlKey);

//                 bitmap = QueryUtils.getImageBitmap(PhotoFragment.photoUrl);
//                 if (bitmap == null) {
//                     remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher);
//                 } else {
//                     remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);
//                 }

//                bitmap = getImageBitmap(PhotoFragment.photoUrl);


                Uri photoUri = Uri.parse(PhotoFragment.photoUrl);
//                Picasso.get().load(photoUri).into(R.id.widget_image);

                final int radius = 5;
                final int margin = 5;



                Picasso.get().load(photoUri)
                        .into(
                        remoteViews, R.id.widget_image, new int [] {
                             appWidgetId
                        }
                );
//
//                remoteViews.setImageViewUri(R.id.widget_image, Uri.parse(""));
//                remoteViews.setImageViewUri(R.id.widget_image, Uri.parse(PhotoFragment.photoUrl));
                remoteViews.setTextViewText(R.id.widget_image_title, PhotoFragment.photoTitle);
                remoteViews.setViewVisibility(R.id.widget_image_title, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.widget_image_label, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE);

            } else {
                remoteViews.setViewVisibility(R.id.widget_image_label, View.GONE);
                remoteViews.setViewVisibility(R.id.widget_image_title, View.GONE);
                remoteViews.setViewVisibility(R.id.widget_image, View.VISIBLE);
                remoteViews.setImageViewResource(R.id.widget_image, R.mipmap.ic_launcher);
            }
            remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName appWidget = new ComponentName(context.getPackageName(), AstroAppWidget.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidget);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_label);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_image_title);
            }
        }
    }


}
