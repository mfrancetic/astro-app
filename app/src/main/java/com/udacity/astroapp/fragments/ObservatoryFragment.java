package com.udacity.astroapp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.PrimitiveIterator;

public class ObservatoryFragment extends Fragment {

    private Observatory observatory;

    private String observatoryId;

    private MainActivity mainActivity;

    private String observatoryName;

    private String observatoryAddress;

    private double observatoryLatitude;

    private double observatoryLongitude;

    private String observatoryUrl;

    private String observatoryPhotoUrl;

    private String observatoryPhoneNumber;

    private List<String> observatoryOpeningHours;

    private String observatoryOpeningHoursDay;

    private boolean observatoryOpenNow;

    private TextView observatoryNameTextView;

    private TextView observatoryAddressTextView;

    private TextView observatoryOpenNowTextView;

    private ImageView observatoryImageView;

    private Button visitObservatoryHomepageButton;

    private static final String LOG_TAG = ObservatoryFragment.class.getSimpleName();


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
            observatoryId = observatory.getObservatoryId();
        }

        observatoryOpeningHoursDay = "";

        if (getActivity() != null) {
            getActivity().setTitle(observatoryName);
        }

        observatoryNameTextView = rootView.findViewById(R.id.observatory_name);
        observatoryAddressTextView = rootView.findViewById(R.id.observatory_address);
        observatoryOpenNowTextView = rootView.findViewById(R.id.observatory_opening_hours);
        observatoryImageView = rootView.findViewById(R.id.observatory_image_view);
        visitObservatoryHomepageButton = rootView.findViewById(R.id.observatory_visit_homepage_button);

        new ObservatoryDetailsAsyncTask().execute();

        populateObservatory();


        return rootView;
    }

    public void setObservatory(Observatory observatory) {
        this.observatory = observatory;
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

    private class ObservatoryDetailsAsyncTask extends AsyncTask<String, Void, Observatory> {


        @Override
        protected Observatory doInBackground(String... strings) {

            try {
                URL url = QueryUtils.createObservatoryDetailsUrl(observatoryId);
                String observatoryDetailsJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseObservatoryJsonResponse = new JSONObject(observatoryDetailsJson);

                JSONObject observatoryObject = baseObservatoryJsonResponse.getJSONObject("result");
//                JSONArray observatoryDetailsArray = baseObservatoryJsonResponse.getJSONArray("result");

//                for (int i = 0; i < observatoryDetailsArray.length(); i++) {

                if (observatoryObject.has("website")) {
                    observatoryUrl = observatoryObject.getString("website");
                    observatory.setObservatoryUrl(observatoryUrl);
                }

                if (observatoryObject.has("international_phone_number")) {
                    observatoryPhoneNumber = observatoryObject.getString("international_phone_number");
                    observatory.setObservatoryPhoneNumber(observatoryPhoneNumber);
                }

                if (observatoryObject.has("opening_hours")) {
                    JSONObject openingHoursJsonObject = observatoryObject.getJSONObject("opening_hours");

                    JSONArray openingHoursArray = openingHoursJsonObject.getJSONArray("weekday_text");

                    for (int i= 0; i<openingHoursArray.length(); i++) {

                        observatoryOpeningHoursDay = observatoryOpeningHoursDay + "\n" + openingHoursArray.getString(i);
//                           observatoryOpeningHoursDay =  observatoryOpeningHoursDay.concat(openingHoursArray.getString(i));

//                        observatoryOpeningHoursDay = observatoryOpeningHoursDay.concat(openingHoursArray.getString(i));
//                        observatoryOpeningHoursDay = observatoryOpeningHoursDay + openingHoursArray.getString(i);
                        observatory.setObservatoryOpeningHours(observatoryOpeningHoursDay);
                    }
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the observatory details JSON results");
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the observatory details JSON response");

            }
            return observatory;
        }

        @Override
        protected void onPostExecute(Observatory observatory) {
            if (observatory != null) {
                populateObservatory();
            } else {

            }
        }
    }

}
