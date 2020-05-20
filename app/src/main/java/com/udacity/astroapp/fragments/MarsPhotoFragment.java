package com.udacity.astroapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.udacity.astroapp.R;
import com.udacity.astroapp.models.MarsPhoto;
import com.udacity.astroapp.utils.Constants;
import com.udacity.astroapp.utils.MarsPhotoService;
import com.udacity.astroapp.utils.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarsPhotoFragment extends Fragment {

    @BindView(R.id.mars_photo_loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.mars_photo_empty_image_view)
    ImageView emptyImageView;

    @BindView(R.id.mars_photo_empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.mars_photo_date_text_view)
    TextView dateTextView;

    @BindView(R.id.mars_photo_launch_date_text_view)
    TextView launchDateTextView;

    @BindView(R.id.mars_photo_landing_date_text_view)
    TextView landingDateTextView;

    @BindView(R.id.mars_photo_camera_text_view)
    TextView cameraTextView;

    @BindView(R.id.mars_photo_source_text_view)
    TextView sourceTextView;

    @BindView(R.id.mars_photo_view)
    ImageView photoView;

    @BindView(R.id.mars_photo_previous_button)
    ImageButton previousButton;

    @BindView(R.id.mars_photo_next_button)
    ImageButton nextButton;

    @BindView(R.id.fab_mars_photo)
    FloatingActionButton fab;

    private MarsPhotoService marsPhotoService;
    private List<MarsPhoto> marsPhotos = new ArrayList<>();

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.menu_mars_photo));
        }
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_mars_photo, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

        setupLoadingView();
        getPhotoDateFromUrl();

        return rootView;
    }

    private void getPhotoDateFromUrl() {
        marsPhotoService = RetrofitClientInstance.getRetrofitInstance().create(MarsPhotoService.class);
        Call<List<MarsPhoto>> marsPhotoCalls = marsPhotoService.marsPhotoList("2020-5-20", Constants.NASA_API_KEY,
                Constants.PAGE_NUMBER);

        marsPhotoCalls.enqueue(new Callback<List<MarsPhoto>>() {
            @Override
            public void onResponse(Call<List<MarsPhoto>> call, Response<List<MarsPhoto>> response) {
                landingDateTextView.setText(response.body().toString());
            }

            @Override
            public void onFailure(Call<List<MarsPhoto>> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLoadingView() {
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyImageView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        dateTextView.setVisibility(View.GONE);
        cameraTextView.setVisibility(View.GONE);
        launchDateTextView.setVisibility(View.GONE);
        landingDateTextView.setVisibility(View.GONE);
        sourceTextView.setVisibility(View.GONE);
        fab.hide();
    }
}