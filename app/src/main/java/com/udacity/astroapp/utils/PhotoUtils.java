package com.udacity.astroapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.udacity.astroapp.R;
import com.udacity.astroapp.data.GlideApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        ImageView fullScreenImageView = dialog.findViewById(R.id.photo_full_screen_view);

        GlideApp.
                with(context)
                .load(photoUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public static void sharePhoto(Context context, String photoUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Glide.
                with(context)
                .asBitmap()
                .load(photoUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(context, resource));
                        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_photo_content_description)));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private static Uri getLocalBitmapUri(Context context, Bitmap bitmap) {
        Uri bitmapUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();
            bitmapUri = FileProvider.getUriForFile(context, context.getApplicationContext()
                    .getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmapUri;
    }
}