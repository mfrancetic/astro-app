package com.udacity.astroapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.ObservatoryViewModel;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.models.Observatory;

import java.util.List;

public class ObservatoryAdapter extends RecyclerView.Adapter<ObservatoryAdapter.ObservatoryViewHolder> {

    public class ObservatoryViewHolder extends RecyclerView.ViewHolder {

        private TextView observatoryListItemNameTextView;

        private TextView observatoryListItemAddressTextView;

        private TextView observatoryListItemOpeningHoursTextView;

        private ImageButton observatoryListItemButton;



        public ObservatoryViewHolder(@NonNull View itemView) {
            super(itemView);
            observatoryListItemNameTextView = itemView.findViewById(R.id.observatory_list_item_name);
            observatoryListItemAddressTextView = itemView.findViewById(R.id.observatory_list_item_address);
            observatoryListItemOpeningHoursTextView = itemView.findViewById(R.id.observatory_list_item_opening_Hours);
            observatoryListItemButton = itemView.findViewById(R.id.observatory_list_item_button);


        }
    }

    private List<Observatory> observatories;

    private boolean observatoryOpenNow;

    private Context context;


    public ObservatoryAdapter(List<Observatory> observatories, ObservatoryListFragment.OnObservatoryClickListener onObservatoryClickListener) {
        this.observatories = observatories;
        ObservatoryListFragment.onObservatoryClickListener = onObservatoryClickListener;
    }

    @NonNull
    @Override
    public ObservatoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View observatoryListView = layoutInflater.inflate(R.layout.observatory_list_item, parent, false);
        return new ObservatoryViewHolder(observatoryListView);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservatoryViewHolder viewHolder, int position) {
        Observatory observatory = observatories.get(position);


        TextView observatoryListItemNameTextView = viewHolder.observatoryListItemNameTextView;
        TextView observatoryListItemAddressTextView = viewHolder.observatoryListItemAddressTextView;
        TextView observatoryListItemOpeningHoursTextView = viewHolder.observatoryListItemOpeningHoursTextView;
        ImageButton  observatoryListItemButton = viewHolder.observatoryListItemButton;

//        final Context context = observatoryListItemNameTextView.getContext();

        observatoryListItemNameTextView.setText(observatory.getObservatoryName());
        observatoryListItemAddressTextView.setText(observatory.getObservatoryAddress());

        observatoryOpenNow = observatory.getObservatoryOpenNow();
        if (observatoryOpenNow) {
            observatoryListItemOpeningHoursTextView.setText(R.string.observatory_open);
        }
//        else {
//            observatoryListItemOpeningHoursTextView.setText(R.string.observatory_closed);
//        }

//        observatoryListItemOpeningHoursTextView.setText(String.valueOf()observatory.getObservatoryOpenNow());
        viewHolder.observatoryListItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ObservatoryListFragment.onObservatoryClickListener == null) {
                    ObservatoryListFragment.onObservatoryClickListener = new ObservatoryListFragment.OnObservatoryClickListener() {
                        @Override
                        public void onObservatorySelected(int position) {
//                        onObservatorySelected(position);
                        }
                    };
                }
                    ObservatoryListFragment.onObservatoryClickListener.onObservatorySelected(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (observatories == null) {
            return 0;
        } else {
            return observatories.size();
        }
    }

    public void setObservatories(List<Observatory> observatories) {
        this.observatories = observatories;
        notifyDataSetChanged();
    }
}
