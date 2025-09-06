package com.udacity.astroapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "photo")
@Parcelize
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val photoId: Int = 0,
    val photoTitle: String? = null,
    val photoDate: String,
    val photoDescription: String? = null,
    val photoUrl: String? = null,
    val photoMediaType: String? = null,
    val cacheTimestamp: Long = System.currentTimeMillis()
) : Parcelable