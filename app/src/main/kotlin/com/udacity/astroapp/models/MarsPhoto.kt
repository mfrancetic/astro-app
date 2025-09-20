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
    @SerializedName("id") @PrimaryKey val id: Int = 0,
    @SerializedName("sol") val sol: String = "",
    @SerializedName("img_src") val imageUrl: String = "",
    @SerializedName("earth_date") val earthDate: String = "",
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "camera")
    @SerializedName("camera")
    val camera: Camera? = null,
    @TypeConverters(Converters::class)
    @ColumnInfo(name = "rover")
    @SerializedName("rover")
    val rover: Rover? = null
) : Parcelable
