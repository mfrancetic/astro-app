package com.udacity.astroapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.udacity.astroapp.R;
import com.udacity.astroapp.data.GlideApp;

public class PhotoUtils {

    public static void displayPhotoFromUrl(Context context, Uri photoUri, ImageView photoImageView) {
        Glide.with(context)
                .load(photoUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.mipmap.ic_launcher)
                .centerCrop()
                .into(photoImageView);
    }

    public static void displayPhotoDialog(Context context, Uri photoUri) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.fullscreen);
        dialog.show();

//        boolean isDialogShown = true;

        ImageView fullScreenImageView = dialog.findViewById(R.id.photo_full_screen_view);

        GlideApp.
                with(context)
                .load(photoUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isDialogShown = false;
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(photoDialog -> {
//            isDialogShown = false;
        });
//        return isDialogShown;
    }
}