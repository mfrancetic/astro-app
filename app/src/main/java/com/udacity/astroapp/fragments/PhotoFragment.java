package com.udacity.astroapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.DefaultHlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PhotoFragment extends Fragment implements Player.EventListener{

    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    private ImageView photoImageView;

    private TextView photoTitleTextView;

    private TextView photoDateTextView;

    private TextView photoDescriptionTextView;

    private Context context;

    private FrameLayout playerPhotoViewFrame;

    private PlayerView exoPlayerView;

    private SimpleExoPlayer exoPlayer;

    private static MediaSessionCompat mediaSession;

    private PlaybackStateCompat.Builder stateBuilder;

    private boolean exoPlayerIsFullScreen = false;

    private ScrollView photoScrollView;

    private String videoUrl;

    private static Uri videoUri;

    private Uri photoUri;

    private TextView emptyTextView;

    private ProgressBar loadingIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_photo);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        photoImageView = rootView.findViewById(R.id.photo_view);
        photoTitleTextView = rootView.findViewById(R.id.photo_title_text_view);
        photoDateTextView = rootView.findViewById(R.id.photo_date_text_view);
        exoPlayerView = rootView.findViewById(R.id.player_view);

        photoScrollView = rootView.findViewById(R.id.photo_scroll_view);
        playerPhotoViewFrame = rootView.findViewById(R.id.player_photo_view_frame);

        loadingIndicator = rootView.findViewById(R.id.photo_loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyTextView = rootView.findViewById(R.id.photo_empty_text_view);

        emptyTextView.setVisibility(View.GONE);

        context = photoDateTextView.getContext();

        if (photoScrollView != null) {
            photoScrollView.requestFocus();
        }

        releasePlayer();

        initializeMediaSession();

        photoDescriptionTextView = rootView.findViewById(R.id.photo_description_text_view);

//        videoUri = Uri.parse(videoUrl);

        new PhotoAsyncTask().execute();


        return rootView;
    }

    private class PhotoAsyncTask extends AsyncTask<String, Void, Photo> {

        Photo photo;

        @Override
        protected Photo doInBackground(String... strings) {

            try {
                URL url = QueryUtils.createPhotoUrl();
                String photoJson = QueryUtils.makeHttpRequest(url);

                JSONObject photoObject = new JSONObject(photoJson);

                String photoTitle = photoObject.getString("title");

                String photoDate = photoObject.getString("date");

                String photoDescription = photoObject.getString("explanation");

                String photoUrl = photoObject.getString("url");

                String photoMediaType = photoObject.getString("media_type");

                int id = 0;

                photo = new Photo(id, photoTitle, photoDate, photoDescription, photoUrl, photoMediaType);

                photo.setPhotoId(id);
                photo.setPhotoTitle(photoTitle);
                photo.setPhotoDate(photoDate);
                photo.setPhotoDescription(photoDescription);
                photo.setPhotoUrl(photoUrl);
                photo.setPhotoMediaType(photoMediaType);
            } catch (
                    IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
            } catch (
                    JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON resposne");
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Photo photo) {
            if(photo != null) {
                populatePhoto(photo);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                photoTitleTextView.setVisibility(View.GONE);
                photoDescriptionTextView.setVisibility(View.GONE);
                photoDateTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                exoPlayerView.setVisibility(View.GONE);
            }
            super.onPostExecute(photo);
        }

        private void populatePhoto(Photo photo) {
            emptyTextView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);

            photoImageView.setVisibility(View.VISIBLE);
            photoDateTextView.setVisibility(View.VISIBLE);
            photoTitleTextView.setVisibility(View.VISIBLE);
            photoDescriptionTextView.setVisibility(View.VISIBLE);

            photoTitleTextView.setText(photo.getPhotoTitle());
            photoDateTextView.setText(photo.getPhotoDate());
            photoDescriptionTextView.setText(photo.getPhotoDescription());

            videoUrl = "https://www.ustream.tv/embed/17074538?v=3&wmode=direct";


//            if (photo.getPhotoMediaType().equals("video")) {
//                videoUrl = photo.getPhotoUrl();
                videoUri = Uri.parse(videoUrl);
                initializePlayer(videoUri);
                //TODO return
//            } else if (photo.getPhotoMediaType().equals("image")) {
//                photoUri = Uri.parse(photo.getPhotoUrl());
//
//                Picasso.get().load(photoUri)
//                        .into(photoImageView);
//            }
        }

//        private void populateVideo() {
//            releasePlayer();
//            initializeMediaSession();
//            initializePlayer(videoUri);
//        }
    }

    /**
     * Initialize a new MediaSession
     */
    private void initializeMediaSession() {
        mediaSession = new MediaSessionCompat(getActivity(), LOG_TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);

        /* Create a new stateBuilder with the play, pause and play_pause actions */
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new SessionCallback());
        mediaSession.setActive(true);
    }

    private class SessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            /* Set playWhenReady to true */
            exoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            /* Set playWhenReady to false */
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializePlayer(videoUri);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializePlayer(videoUri);
    }

    public static class MediaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }
    }

    private void initializePlayer(Uri videoUri) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            exoPlayerView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
        } else {
            exoPlayerView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);

            if (exoPlayer == null) {

                exoPlayer = ExoPlayerFactory.newSimpleInstance(context);

                exoPlayerView.setPlayer(exoPlayer);

                exoPlayer.addListener(this);

                String userAgent = Util.getUserAgent(context, getString(R.string.app_name));

                DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
                DefaultHlsDataSourceFactory hlsDataSourceFactory = new DefaultHlsDataSourceFactory(httpDataSourceFactory);

                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

//);

                HlsMediaSource mediaSource = new HlsMediaSource.Factory(hlsDataSourceFactory)
                        .createMediaSource(videoUri);

//                DashMediaSource mediaSource = new DashMediaSource.Factory(httpDataSourceFactory)
//                        .createMediaSource(videoUri);


//                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

//                MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(context, userAgent))
//                        .setExtractorsFactory(extractorsFactory).createMediaSource(videoUri);

//                exoPlayer.prepare(exoPlayer.Create);
                exoPlayer.prepare(mediaSource);
                exoPlayer.setPlayWhenReady(true);
            }
        }
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mediaSession.setActive(false);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == Player.STATE_READY) && playWhenReady) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    exoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == Player.STATE_READY)) {
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.getCurrentPosition(), 1f);
        }
        mediaSession.setPlaybackState(stateBuilder.build());
    }
}