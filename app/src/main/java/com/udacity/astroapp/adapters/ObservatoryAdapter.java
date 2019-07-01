package com.udacity.astroapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.models.Observatory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * RecyclerView Adapter for displaying the list of observatories in the ObservatoryListFragment
 */
public class ObservatoryAdapter extends RecyclerView.Adapter<ObservatoryAdapter.ObservatoryViewHolder> {

    class ObservatoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.observatory_list_item_name)
        TextView observatoryListItemNameTextView;

        @BindView(R.id.observatory_list_item_address)
        TextView observatoryListItemAddressTextView;

        @BindView(R.id.observatory_list_item_opening_Hours)
        TextView observatoryListItemOpenNowTextView;

        @BindView(R.id.observatory_list_item_button)
        ImageButton observatoryListItemButton;

        ObservatoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static List<Observatory> observatories;
    private Context context;

    public ObservatoryAdapter(List<Observatory> observatories, ObservatoryListFragment.OnObservatoryClickListener onObservatoryClickListener) {
        ObservatoryAdapter.observatories = observatories;
        ObservatoryListFragment.onObservatoryClickListener = onObservatoryClickListener;
    }

    @NonNull
    @Override
    public ObservatoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        /* Inflate the observatory_list_item.xml layout */
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View observatoryListView = layoutInflater.inflate(R.layout.observatory_list_item, parent, false);
        return new ObservatoryViewHolder(observatoryListView);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservatoryViewHolder viewHolder, int position) {
        /* Get the observatory according to its position */
        Observatory observatory = observatories.get(position);

        /* Set the views to its views in the viewHolder */
        TextView observatoryListItemNameTextView = viewHolder.observatoryListItemNameTextView;
        TextView observatoryListItemAddressTextView = viewHolder.observatoryListItemAddressTextView;
        TextView observatoryListItemOpenNowTextView = viewHolder.observatoryListItemOpenNowTextView;

        /* Set the observatoryName to the observatoryListItemNameTextView */
        String observatoryName = observatory.getObservatoryName();
        observatoryListItemNameTextView.setText(observatoryName);

        /* Set the address to the observatoryListItemAddress */
        observatoryListItemAddressTextView.setText(observatory.getObservatoryAddress());

        boolean observatoryOpenNow = observatory.getObservatoryOpenNow();
        /* In case there is information if the observatory is open now, set that text to the
         * observatoryListItemOpenNowTextView */
        if (observatoryOpenNow) {
            observatoryListItemOpenNowTextView.setText(R.string.observatory_open);
        }

        /* Set the content description of the observatoryListItemButton to match the name of the
         * observatory */
        viewHolder.observatoryListItemButton.setContentDescription(context.getString(R.string.observatory_list_item_button_content_description) + " "
                + observatoryName);

        /* Set an OnClickListener to the observatoryListItemButton */
        viewHolder.observatoryListItemButton.setOnClickListener(v -> {
            if (ObservatoryListFragment.onObservatoryClickListener == null) {
                /* In case there is no onObservatoryClickListener, create a new one */
                ObservatoryListFragment.onObservatoryClickListener = position1 -> {
                };
            }
            ObservatoryListFragment.onObservatoryClickListener.onObservatorySelected(position);
        });
    }

    @Override
    public int getItemCount() {
        /* In case there are no observatories, return 0.
         * Otherwise, return the number of the observatories */
        if (observatories == null) {
            return 0;
        } else {
            return observatories.size();
        }
    }

    /**
     * Sets the list of observatories and notifies the adapter that the dataset has changed
     */
    public void setObservatories(List<Observatory> observatories) {
        ObservatoryAdapter.observatories = observatories;
        notifyDataSetChanged();
    }
}