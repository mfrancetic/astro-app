package com.udacity.astroapp.adapters;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.models.Asteroid;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AsteroidAdapter extends RecyclerView.Adapter<AsteroidAdapter.ViewHolder> {


    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView asteroidNameTextView;

        private TextView asteroidDiameterTextView;

        private TextView asteroidApproachDateTextView;

        private TextView asteroidVelocityTextView;

        private Button readMoreButton;

        private ImageView asteroidImageView;

        private ImageView asteroidHazardousImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            asteroidNameTextView = itemView.findViewById(R.id.asteroid_name_text_view);
            asteroidDiameterTextView = itemView.findViewById(R.id.asteroid_diameter_text_view);
            asteroidApproachDateTextView = itemView.findViewById(R.id.asteroid_approach_date_text_view);
            asteroidHazardousImageView = itemView.findViewById(R.id.asteroid_hazardous_image);
            asteroidImageView = itemView.findViewById(R.id.asteroid_image_view);
            asteroidVelocityTextView = itemView.findViewById(R.id.asteroid_velocity_text_view);
            readMoreButton = itemView.findViewById(R.id.asteroid_read_more_button);
        }
    }

    private List<Asteroid> asteroids;

    private String asteroidFullName;

    private Date approachDateObject;

    public AsteroidAdapter(List<Asteroid> asteroids) {
        this.asteroids = asteroids;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View asteroidView = inflater.inflate(R.layout.asteroid_list_item, parent, false);
        return new ViewHolder(asteroidView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Asteroid asteroid = asteroids.get(position);

        TextView asteroidNameTextView = viewHolder.asteroidNameTextView;
        TextView asteroidDiameterTextView = viewHolder.asteroidDiameterTextView;
        TextView asteroidVelocityTextView = viewHolder.asteroidVelocityTextView;
        TextView asteroidApproachDateTextView = viewHolder.asteroidApproachDateTextView;
        ImageView asteroidHazardousImage = viewHolder.asteroidHazardousImageView;
        Button readMoreButton = viewHolder.readMoreButton;

        final Context context = asteroidVelocityTextView.getContext();

        asteroidFullName = asteroid.getAsteroidName();

        asteroidNameTextView.setText(getTrimmedAsteroidName(asteroidFullName));

        double asteroidDiameterMin = asteroid.getAsteroidDiameterMin();
        double asteroidDiameterMax = asteroid.getAsteroidDiameterMax();

        double asteroidDiameterMinDecimal = getDiameterDecimal(asteroidDiameterMin);
        double asteroidDiameterMaxDecimal = getDiameterDecimal(asteroidDiameterMax);

        String asteroidDiameter = asteroidDiameterMinDecimal + " - " + asteroidDiameterMaxDecimal + " km";

        asteroidDiameterTextView.setText(asteroidDiameter);

        String asteroidVelocity = asteroid.getAsteroidVelocity();

        String asteroidVelocityDecimal = getVelocityDecimal(asteroidVelocity) + " km/s";

        asteroidVelocityTextView.setText(asteroidVelocityDecimal);

        String approachDate = asteroid.getAsteroidApproachDate();

        boolean isHazardous = asteroid.getAsteroidIsHazardous();
        if (isHazardous) {
            asteroidHazardousImage.setImageResource(R.drawable.hazardous_image);
        } else {
            asteroidHazardousImage.setImageResource(R.drawable.not_hazardous_image);
        }

        asteroidApproachDateTextView.setText(approachDate);

        String asteroidUrl = asteroid.getAsteroidUrl();
        final Uri asteroidUri = Uri.parse(asteroidUrl);

        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAsteroidDetailsIntent = new Intent(Intent.ACTION_VIEW);
                openAsteroidDetailsIntent.setData(asteroidUri);
                context.startActivity(openAsteroidDetailsIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (asteroids == null) {
            return 0;
        } else {
            return asteroids.size();
        }
    }

    public void setAsteroids(List<Asteroid> asteroids) {
        this.asteroids = asteroids;
        notifyDataSetChanged();
    }

    private String getTrimmedAsteroidName(String asteroidFullName) {
        return asteroidFullName.substring(asteroidFullName.indexOf("(") + 1, asteroidFullName.indexOf(")"));
    }

    private double getDiameterDecimal(double diameterDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(decimalFormat.format(diameterDouble));
    }

    private String getVelocityDecimal(String asteroidVelocity) {
        double asteroidVelocityDouble = Double.parseDouble(asteroidVelocity);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(asteroidVelocityDouble);
    }

}