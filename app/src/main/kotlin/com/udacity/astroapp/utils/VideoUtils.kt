package com.udacity.astroapp.utils

import java.util.regex.Pattern

object VideoUtils {

    /**
     * Extracts YouTube video ID from a YouTube URL
     * Supports various YouTube URL formats
     */
    fun extractYouTubeVideoId(videoUrl: String): String? {
        val pattern = Pattern.compile(
            "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch\\?v=)([^#&?]*).*$",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(videoUrl)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            null
        }
    }

    /**
     * Checks if a URL is likely a YouTube video that might not be playable
     */
    fun isUnplayableVideo(photoUrl: String): Boolean {
        return photoUrl.contains("youtube.com") &&
               (photoUrl.contains("playlist") || photoUrl.contains("channel"))
    }

    /**
     * Checks if the media type indicates video content
     */
    fun isVideoContent(mediaType: String): Boolean {
        return mediaType.equals("video", ignoreCase = true)
    }
}