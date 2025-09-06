package com.udacity.astroapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ImageSharingUtils {
    
    suspend fun shareImage(
        context: Context,
        imageUrl: String,
        title: String? = null,
        text: String? = null
    ) {
        try {
            val bitmap = downloadImage(context, imageUrl)
            val imageUri = saveBitmapToCache(context, bitmap, "shared_image.jpg")
            shareImageUri(context, imageUri, title, text)
        } catch (e: Exception) {
            // Fallback to sharing URL
            shareImageUrl(context, imageUrl, title, text)
        }
    }
    
    private suspend fun downloadImage(context: Context, imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val target = object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        continuation.resume(resource)
                    }
                    
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // No-op
                    }
                    
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        continuation.resumeWithException(Exception("Failed to download image"))
                    }
                }
                
                val request = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(target)
                
                continuation.invokeOnCancellation {
                    Glide.with(context).clear(request)
                }
            }
        }
    }
    
    private suspend fun saveBitmapToCache(
        context: Context, 
        bitmap: Bitmap, 
        filename: String
    ): Uri {
        return withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val file = File(cacheDir, filename)
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }
    }
    
    private fun shareImageUri(
        context: Context,
        imageUri: Uri,
        title: String?,
        text: String?
    ) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            text?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
    }
    
    private fun shareImageUrl(
        context: Context,
        imageUrl: String,
        title: String?,
        text: String?
    ) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            val shareText = if (text != null) {
                "$text $imageUrl"
            } else {
                "Check out this photo: $imageUrl"
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
            title?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
    }
}