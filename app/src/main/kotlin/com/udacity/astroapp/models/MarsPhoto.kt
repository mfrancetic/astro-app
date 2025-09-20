package com.udacity.astroapp.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.udacity.astroapp.utils.Converters
import kotlinx.parcelize.Parcelize

@Entity(tableName = "marsphoto")
@Parcelize
data class MarsPhoto(
    @SerializedName("id") @PrimaryKey val id: Int,
    @SerializedName("sol") val sol: String? = null,
    @SerializedName("img_src") val imageUrl: String? = null,
    @SerializedName("earth_date") val earthDate: String? = null,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "camera")
    @SerializedName("camera")
    val camera: Camera? = null,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "rover")
    @SerializedName("rover")
    val rover: Rover? = null,
    val cacheTimestamp: Long = System.currentTimeMillis()
) : Parcelable
