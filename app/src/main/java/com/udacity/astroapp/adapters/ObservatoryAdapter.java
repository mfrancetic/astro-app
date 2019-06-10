package com.udacity.astroapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.data.ObservatoryViewModel;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.models.Observatory;

import java.util.List;

public class ObservatoryAdapter extends RecyclerView.Adapter<ObservatoryAdapter.ObservatoryViewHolder> {

    public class ObservatoryViewHolder extends RecyclerView.ViewHolder {

        private TextView observatoryListItemNameTextView;

        private TextView observatoryListItemAddressTextView;

        private TextView observatoryListItemOpeningHoursTextView;

        private Button observatoryListItemButton;


        public ObservatoryViewHolder(@NonNull View itemView) {
            super(itemView);
            observatoryListItemNameTextView = itemView.findViewById(R.id.observatory_list_item_name);
            observatoryListItemAddressTextView = itemView.findViewById(R.id.observatory_list_item_address);
            observatoryListItemOpeningHoursTextView = itemView.findViewById(R.id.observatory_list_item_opening_Hours);
            observatoryListItemButton = itemView.findViewById(R.id.observatory_list_item_button);


        }
    }

    private List<Observatory> observatories;

    public ObservatoryAdapter(List<Observatory> observatories) {
        this.observatories = observatories;
    }

    @NonNull
    @Override
    public ObservatoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View observatoryListView = layoutInflater.inflate(R.layout.fragment_observatory_list, parent, false);
        return new ObservatoryViewHolder(observatoryListView);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservatoryViewHolder viewHolder, int position) {
        Observatory observatory = observatories.get(position);

        TextView observatoryListItemNameTextView = viewHolder.observatoryListItemNameTextView;
        TextView observatoryListItemAddressTextView = viewHolder.observatoryListItemAddressTextView;
        TextView observatoryListItemOpeningHoursTextView = viewHolder.observatoryListItemOpeningHoursTextView;
        Button observatoryListItemButton = viewHolder.observatoryListItemButton;

        Context context = observatoryListItemNameTextView.getContext();

        observatoryListItemNameTextView.setText(observatory.getObservatoryName());
        observatoryListItemAddressTextView.setText(observatory.getObservatoryAddress());
        observatoryListItemOpeningHoursTextView.setText(observatory.getObservatoryOpeningHours());
        observatoryListItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openObservatoryDetailsIntent = new Intent(context, ObservatoryFragment.class);
                openObservatoryDetailsIntent.putExtra("observatory", observatory);
                context.startActivity(openObservatoryDetailsIntent);
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
