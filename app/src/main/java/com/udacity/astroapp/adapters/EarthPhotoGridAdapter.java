package com.udacity.astroapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.models.EarthPhoto;
import com.udacity.astroapp.utils.DateTimeUtils;
import com.udacity.astroapp.utils.PhotoUtils;
import com.udacity.astroapp.utils.QueryUtils;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EarthPhotoGridAdapter extends RecyclerView.Adapter<EarthPhotoGridAdapter.EarthPhotoViewHolder> {

    private List<EarthPhoto> photos;
    private Context context;

    public EarthPhotoGridAdapter(List<EarthPhoto> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public EarthPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.image_thumbnail_grid_item, parent, false);
        return new EarthPhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EarthPhotoGridAdapter.EarthPhotoViewHolder holder, int position) {
        EarthPhoto photo = photos.get(position);
        String photoDate = DateTimeUtils.getFormattedDateFromString(photo.getEarthPhotoDateTime());

        ImageView earthPhotoImageView = holder.earthPhotoImageView;
        ProgressBar earthPhotoLoadingIndicator = holder.earthPhotoLoadingIndicator;

        URL earthPhotoUrl = QueryUtils.createEarthPhotoImageUrl(photoDate, photo.getEarthPhotoUrl());
        Uri earthPhotoUri = Uri.parse(earthPhotoUrl.toString());
        PhotoUtils.displayPhotoFromUrl(context, earthPhotoUri, earthPhotoImageView, earthPhotoLoadingIndicator);
    }

    @Override
    public int getItemCount() {
        if (photos != null) {
            return photos.size();
        }
        return 0;
    }

    class EarthPhotoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.earth_photo_image_view)
        ImageView earthPhotoImageView;

        @BindView(R.id.earth_photo_loading_indicator)
        ProgressBar earthPhotoLoadingIndicator;

        public EarthPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}