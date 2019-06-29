package com.udacity.astroapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * RecyclerView Adapter for displaying the list of asteroids in the AsteroidFragment
 */
public class AsteroidAdapter extends RecyclerView.Adapter<AsteroidAdapter.AsteroidViewHolder> {

    class AsteroidViewHolder extends RecyclerView.ViewHolder {

        private TextView asteroidNameTextView;
        private TextView asteroidDiameterTextView;
        private TextView asteroidApproachDateTextView;
        private TextView asteroidVelocityTextView;
        private Button readMoreButton;
        private ImageView asteroidHazardousImageView;
        private TextView diameterLabelTextView;


        AsteroidViewHolder(@NonNull View itemView) {
            super(itemView);
            asteroidNameTextView = itemView.findViewById(R.id.asteroid_name_text_view);
            asteroidDiameterTextView = itemView.findViewById(R.id.asteroid_diameter_text_view);
            asteroidApproachDateTextView = itemView.findViewById(R.id.asteroid_approach_date_text_view);
            asteroidHazardousImageView = itemView.findViewById(R.id.asteroid_hazardous_image);
            asteroidVelocityTextView = itemView.findViewById(R.id.asteroid_velocity_text_view);
            readMoreButton = itemView.findViewById(R.id.asteroid_read_more_button);
            diameterLabelTextView = itemView.findViewById(R.id.asteroid_diameter_label_text_view);
        }
    }

    private List<Asteroid> asteroids;
    private String LOG_TAG = AsteroidAdapter.class.getSimpleName();

    public AsteroidAdapter(List<Asteroid> asteroids) {
        this.asteroids = asteroids;
    }


    @NonNull
    @Override
    public AsteroidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();

        /* Inflate the view of the asteroid_list_item */
        LayoutInflater inflater = LayoutInflater.from(context);
        View asteroidView = inflater.inflate(R.layout.asteroid_list_item, parent, false);
        return new AsteroidViewHolder(asteroidView);
    }

    @Override
    public void onBindViewHolder(@NonNull AsteroidViewHolder viewHolder, int position) {
        /* Get the asteroid according to its position */
        Asteroid asteroid = asteroids.get(position);

        /* Set the views to its views in the viewHolder */
        TextView asteroidNameTextView = viewHolder.asteroidNameTextView;
        TextView asteroidDiameterTextView = viewHolder.asteroidDiameterTextView;
        TextView asteroidVelocityTextView = viewHolder.asteroidVelocityTextView;
        TextView asteroidApproachDateTextView = viewHolder.asteroidApproachDateTextView;
        ImageView asteroidHazardousImage = viewHolder.asteroidHazardousImageView;
        Button readMoreButton = viewHolder.readMoreButton;
        TextView diameterLabelTextView = viewHolder.diameterLabelTextView;

        /* Get context */
        final Context context = asteroidVelocityTextView.getContext();

        /* Get the asteroidTrimmedName and set it to the asteroidNameTextView */
        String asteroidFullName = asteroid.getAsteroidName();
        String asteroidTrimmedName = getTrimmedAsteroidName(asteroidFullName);
        asteroidNameTextView.setText(asteroidTrimmedName);

        /* Get the asteroids diameterMin and diameterMax and put it in a decimal value */
        double asteroidDiameterMin = asteroid.getAsteroidDiameterMin();
        double asteroidDiameterMax = asteroid.getAsteroidDiameterMax();
        double asteroidDiameterMinDecimal = getDiameterDecimal(asteroidDiameterMin);
        double asteroidDiameterMaxDecimal = getDiameterDecimal(asteroidDiameterMax);

        if (asteroidDiameterMinDecimal != 0.00 && asteroidDiameterMaxDecimal != 0.00) {
            /* In case there are asteroidDiameterMinDecimal and asteroidDiameterMaxDecimal values,
             * set the text of the diameterLabelView and set the view to visible */
            String asteroidDiameter = asteroidDiameterMinDecimal + " - " + asteroidDiameterMaxDecimal + " km";
            asteroidDiameterTextView.setText(asteroidDiameter);
            diameterLabelTextView.setVisibility(View.VISIBLE);
        } else {
            /* In case there are no values, set the visibility of the diameterLabelTextView to gone */
            diameterLabelTextView.setVisibility(View.GONE);
        }

        /* Set the asteroidVelocity value to the asteroidVelocityTextView */
        String asteroidVelocity = asteroid.getAsteroidVelocity();
        String asteroidVelocityDecimal = getVelocityDecimal(asteroidVelocity) + " km/s";
        if (!asteroidVelocityDecimal.equals("0.00")) {
            asteroidVelocityTextView.setText(asteroidVelocityDecimal);
        }

        /* Set the approachDate to the asteroidApproachDateTextView */
        String approachDate = asteroid.getAsteroidApproachDate();
        asteroidApproachDateTextView.setText(approachDate);

        /* Check if the asteroid is hazardous and set the appropriate image resource and
         * content description */
        boolean isHazardous = asteroid.getAsteroidIsHazardous();
        if (isHazardous) {
            asteroidHazardousImage.setImageResource(R.drawable.hazardous_image);
            asteroidHazardousImage.setContentDescription(context.getString(R.string.asteroid_is_hazardous_content_descriptions));
        } else {
            asteroidHazardousImage.setImageResource(R.drawable.not_hazardous_image);
            asteroidHazardousImage.setContentDescription(context.getString(R.string.asteroid_not_hazardous_content_description));
        }

        /* Get and parse the asteroidUrl */
        String asteroidUrl = asteroid.getAsteroidUrl();
        final Uri asteroidUri = Uri.parse(asteroidUrl);

        /* Set the content description of the readMoreButton to indicate the name of the asteroid */
        readMoreButton.setContentDescription(context.getString(R.string.read_more_about_content_description)
                + " " + asteroidTrimmedName);

        /* Set an OnClickListener to the readMoreButton */
        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* OnClick, create and start an intent that opens details of the selected asteroid */
                Intent openAsteroidDetailsIntent = new Intent(Intent.ACTION_VIEW);
                openAsteroidDetailsIntent.setData(asteroidUri);
                context.startActivity(openAsteroidDetailsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        /* In case there are no asteroids, return 0.
         * Otherwise, return the number of the asteroids */
        if (asteroids == null) {
            return 0;
        } else {
            return asteroids.size();
        }
    }

    /**
     * Sets the list of asteroids and notifies the adapter that the dataset has changed
     */
    public void setAsteroids(List<Asteroid> asteroids) {
        this.asteroids = asteroids;
        notifyDataSetChanged();
    }

    /**
     * Trims the asteroid's name
     */
    private String getTrimmedAsteroidName(String asteroidFullName) {
        return asteroidFullName.substring(asteroidFullName.indexOf("(") + 1, asteroidFullName.indexOf(")"));
    }

    /**
     * Converts the diameterDouble in a decimal number
     */
    private double getDiameterDecimal(double diameterDouble) {
        try {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            return Double.parseDouble(decimalFormat.format(diameterDouble));
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Problem parsing the diameterDouble");
        }
        return 0.00;
    }

    /**
     * Converts the asteroidVelocity String in a decimal number
     */
    private String getVelocityDecimal(String asteroidVelocity) {
        try {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            double asteroidVelocityDouble = Double.parseDouble(asteroidVelocity);
            return decimalFormat.format(asteroidVelocityDouble);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Problem parsing the asteroidVelocity String");
            return "0.00";
        }
    }
}