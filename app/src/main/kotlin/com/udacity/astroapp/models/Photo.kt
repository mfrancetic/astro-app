package com.udacity.astroapp.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "photo")
@Parcelize
data class Photo(
    @PrimaryKey(autoGenerate = true) val photoId: Int = 0,
    val photoTitle: String = "",
    val photoDate: String = "",
    val photoDescription: String = "",
    val photoUrl: String = "",
    val photoMediaType: String = ""
) : Parcelable {

    @Ignore
    constructor(
        photoDate: String
    ) : this(
        photoId = 0,
        photoTitle = "",
        photoDate = photoDate,
        photoDescription = "",
        photoUrl = "",
        photoMediaType = ""
    )
}
