package com.udacity.astroapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.models.Observatory;

public class ObservatoryFragment extends Fragment {

    private Observatory observatory;

    private int observatoryId;

    private MainActivity mainActivity;

    private String observatoryName;

    private String observatoryAddress;

    private double observatoryLatitude;

    private double observatoryLongitude;

    private String observatoryUrl;

    private String observatoryPhotoUrl;

    private boolean observatoryOpenNow;

    private TextView observatoryNameTextView;

    private TextView observatoryAddressTextView;

    private TextView observatoryOpenNowTextView;

    private ImageView observatoryImageView;

    private Button visitObservatoryHomepageButton;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getActivity()!= null) {
//            getActivity().setTitle(observatoryName);
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_observatory, container, false);

        if (observatory != null) {
            observatoryName = observatory.getObservatoryName();
            observatoryAddress = observatory.getObservatoryAddress();
            observatoryLatitude = observatory.getObservatoryLatitude();
            observatoryLongitude = observatory.getObservatoryLongitude();
            observatoryUrl = observatory.getObservatoryUrl();
            observatoryPhotoUrl = observatory.getObservatoryPhotoUrl();
            observatoryOpenNow = observatory.getObservatoryOpenNow();
        }

        if (getActivity() != null) {
            getActivity().setTitle(observatoryName);
        }

        observatoryNameTextView = rootView.findViewById(R.id.observatory_name);
        observatoryAddressTextView = rootView.findViewById(R.id.observatory_address);
        observatoryOpenNowTextView = rootView.findViewById(R.id.observatory_opening_hours);
        observatoryImageView = rootView.findViewById(R.id.observatory_image_view);
        visitObservatoryHomepageButton = rootView.findViewById(R.id.observatory_visit_homepage_button);

        populateObservatory();


        return rootView;
    }

    public void setObservatory(Observatory observatory) {
        this.observatory = observatory;
    }

    public void setObservatoryId(int id) {
        this.observatoryId = id;
    }

    public int getObservatoryId() {
        return observatoryId;
    }

    public Observatory getObservatory() {
        return observatory;
    }

    private void populateObservatory() {
        observatoryNameTextView.setText(observatoryName);
        observatoryAddressTextView.setText(observatoryAddress);

        if (observatoryPhotoUrl != null) {
            Uri observatoryPhotoUri = Uri.parse(observatoryPhotoUrl);
            Picasso.get().load(observatoryPhotoUri).into(observatoryImageView);
        }

        if (observatoryOpenNow) {
            observatoryOpenNowTextView.setText(R.string.observatory_open);
        }

        if (observatoryUrl == null || observatoryUrl.isEmpty()) {
            visitObservatoryHomepageButton.setVisibility(View.GONE);
        } else {
            visitObservatoryHomepageButton.setVisibility(View.VISIBLE);
            visitObservatoryHomepageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openObservatoryHomepageIntent = new Intent(Intent.ACTION_VIEW);
                    Uri observatoryHomepageUri = Uri.parse(observatoryUrl);
                    openObservatoryHomepageIntent.setData(observatoryHomepageUri);
                    startActivity(openObservatoryHomepageIntent);
                }
            });
        }
    }


}
