package com.udacity.astroapp.utils

import android.content.Context
import android.content.Intent
import com.udacity.astroapp.models.EarthPhoto
import com.udacity.astroapp.models.MarsPhoto
import com.udacity.astroapp.models.Photo

object PhotoSharingUtils {

    fun sharePhoto(context: Context, photo: Photo) {
        val shareText = buildString {
            append("Check out this amazing astronomy photo")
            photo.photoTitle?.let { title -> append(": $title") }
            appendLine()

            photo.photoUrl?.let { url -> appendLine(url) }
        }

        shareContent(context, shareText, "Share Astronomy Photo")
    }

    fun shareEarthPhoto(context: Context, earthPhoto: EarthPhoto) {
        val shareText = buildString {
            append("Earth from space captured on ${earthPhoto.earthPhotoDateTime}")
            appendLine(earthPhoto.earthPhotoUrl)
        }

        shareContent(context, shareText, "Share Earth Photo")
    }

    fun shareMarsPhoto(context: Context, marsPhoto: MarsPhoto) {
        val shareText = buildString {
            append("Mars photo captured on ${marsPhoto.earthDate}")
            marsPhoto.imageUrl?.let { url -> appendLine(url) }
        }

        shareContent(context, shareText, "Share Mars Photo")
    }

    private fun shareContent(context: Context, text: String, title: String) {
        try {
            val shareIntent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                }

            val chooserIntent = Intent.createChooser(shareIntent, title)
            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            // Handle sharing failure gracefully
            // Could show a toast or log the error
        }
    }
}
