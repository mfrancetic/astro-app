package com.udacity.astroapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object WebIntentUtils {

    fun openWebsiteFromStringUrl(context: Context, url: String) {
        val uri = Uri.parse(url)
        val openWebsiteIntent = Intent(Intent.ACTION_VIEW).apply { data = uri }
        context.startActivity(openWebsiteIntent)
    }
}
