package com.udacity.astroapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.udacity.astroapp.R;
import com.udacity.astroapp.utils.PhotoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EarthPhotoFragment extends Fragment {

    @BindView(R.id.earth_photo_coordinator_layout)
     CoordinatorLayout earthPhotoCoordinatorLayout;

    @BindView(R.id.earth_photo_view)
     ImageView earthPhotoView;

    @BindView(R.id.earth_photo_caption_text_view)
     TextView earthPhotoCaptionTextView;

    @BindView(R.id.earth_photo_date_time_text_view)
     TextView earthPhotoDateTimeTextView;

    @BindView(R.id.earth_photo_source_text_view)
    TextView earthPhotoSourceTextView;

    @BindView(R.id.earth_photo_fab)
    FloatingActionButton earthPhotoFab;

    @BindView(R.id.earth_photo_empty_image_view)
    ImageView earthPhotoEmptyImageView;

    @BindView(R.id.earth_photo_empty_text_view)
    TextView earthPhotoEmptyTextView;

    @BindView(R.id.earth_photo_loading_indicator)
    ProgressBar earthPhotoLoadingIndicator;

    @BindView(R.id.earth_photo_previous_button)
    ImageButton earthPhotoPreviousButton;

    @BindView(R.id.earth_photo_next_button)
    ImageButton earthPhotoNextButton;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_earth_photo);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_earth_photo, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

        setLoadingView();
        displayPhoto();

        return rootView;
    }

    private void displayPhoto() {
        PhotoUtils.displayPhotoFromUrl(context, Uri.parse("https://api.nasa.gov/EPIC/archive/natural/2019/05/30/png/epic_1b_20190530011359.png?api_key=DEMO_KEY"), earthPhotoView);
        earthPhotoView.setVisibility(View.VISIBLE);
        earthPhotoLoadingIndicator.setVisibility(View.GONE);
        earthPhotoSourceTextView.setVisibility(View.VISIBLE);
        earthPhotoCaptionTextView.setText(getString(R.string.earth_photo_caption));
        earthPhotoCaptionTextView.setVisibility(View.VISIBLE);
        earthPhotoDateTimeTextView.setVisibility(View.VISIBLE);
        earthPhotoDateTimeTextView.setText("2019-05-30  14:01:05");
    }

    private void setLoadingView() {
        earthPhotoLoadingIndicator.setVisibility(View.VISIBLE);
        earthPhotoPreviousButton.setVisibility(View.GONE);
        earthPhotoNextButton.setVisibility(View.GONE);
        earthPhotoEmptyImageView.setVisibility(View.GONE);
        earthPhotoEmptyTextView.setVisibility(View.GONE);
        earthPhotoView.setVisibility(View.GONE);
        earthPhotoFab.hide();
        earthPhotoSourceTextView.setVisibility(View.GONE);
    }
}