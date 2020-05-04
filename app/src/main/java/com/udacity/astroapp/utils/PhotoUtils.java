package com.udacity.astroapp.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.udacity.astroapp.R;

public class PhotoUtils {

    public static void displayPhotoFromUrl(Context context, Uri photoUri, ImageView photoImageView) {
        Glide.with(context)
                .load(photoUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.mipmap.ic_launcher)
                .centerCrop()
                .into(photoImageView);
    }
}