package com.udacity.astroapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class WebIntentUtils {

    public static void openWebsiteFromStringUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent openAsteroidDetailsIntent = new Intent(Intent.ACTION_VIEW);
        openAsteroidDetailsIntent.setData(uri);
        context.startActivity(openAsteroidDetailsIntent);
    }
}