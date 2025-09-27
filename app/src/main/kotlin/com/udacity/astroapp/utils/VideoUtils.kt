package com.udacity.astroapp.utils

import android.util.Log
import java.util.regex.Pattern

object VideoUtils {

    /** Extracts YouTube video ID from a YouTube URL Supports various YouTube URL formats */
    fun extractYouTubeVideoId(videoUrl: String): String? {
        Log.d("VideoUtils", "Extracting video ID from URL: $videoUrl")

        val patterns =
            listOf(
                // Standard watch URL: https://www.youtube.com/watch?v=VIDEO_ID
                Pattern.compile(
                    "^https?://(?:www\\.)?youtube\\.com/watch\\?v=([^#&?]+).*$",
                    Pattern.CASE_INSENSITIVE
                ),
                // Embed URL: https://www.youtube.com/embed/VIDEO_ID
                Pattern.compile(
                    "^https?://(?:www\\.)?youtube\\.com/embed/([^#&?]+).*$",
                    Pattern.CASE_INSENSITIVE
                ),
                // Short URL: https://youtu.be/VIDEO_ID
                Pattern.compile("^https?://youtu\\.be/([^#&?]+).*$", Pattern.CASE_INSENSITIVE),
                // Other formats
                Pattern.compile(
                    "^https?://(?:www\\.)?youtube\\.com/(?:v/|u/\\w/)([^#&?]+).*$",
                    Pattern.CASE_INSENSITIVE
                )
            )

        for (pattern in patterns) {
            val matcher = pattern.matcher(videoUrl)
            if (matcher.matches()) {
                val videoId = matcher.group(1)
                Log.d("VideoUtils", "Successfully extracted video ID: $videoId")
                return videoId
            }
        }

        Log.w("VideoUtils", "Could not extract video ID from URL: $videoUrl")
        return null
    }

    /** Checks if the media type indicates video content */
    fun isVideoContent(mediaType: String): Boolean {
        return mediaType.equals("video", ignoreCase = true)
    }
}
