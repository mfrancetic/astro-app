package com.udacity.astroapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object WebIntentUtils {
    
    fun openWebsiteFromStringUrl(context: Context, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    fun shareText(context: Context, text: String, title: String = "Share") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        val chooser = Intent.createChooser(intent, title)
        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        }
    }
    
    fun shareImage(context: Context, imageUri: Uri, text: String? = null) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            text?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share Image")
        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        }
    }
}