package com.dglozano.escale.ui.main.home;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.ui.MeasurementItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MeasurementListAdapter extends RecyclerView.Adapter<MeasurementListAdapter.MeasurementViewHolder> {

    @BindDrawable(R.drawable.home_ic_weight_kg)
    Drawable weightIcon;
    @BindDrawable(R.drawable.home_ic_drop_colored)
    Drawable waterIcon;
    @BindDrawable(R.drawable.home_ic_pizza_slice_colored)
    Drawable pizzaIcon;
    @BindDrawable(R.drawable.home_ic_bmi_colored)
    Drawable bmiIcon;
    @BindDrawable(R.drawable.home_ic_muscle_colored)
    Drawable muscleIcon;

    private List<MeasurementItem> mMeasurementItems;

    @Inject
    public MeasurementListAdapter(MainActivity mainActivity) {
        mMeasurementItems = new ArrayList<>();
        ButterKnife.bind(this, mainActivity);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MeasurementViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                    int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_measurement, parent, false);

        return new MeasurementViewHolder(itemView);
    }

    public void setItems(List<MeasurementItem> measurementItemList) {
        mMeasurementItems.clear();
        mMeasurementItems.addAll(measurementItemList);
        notifyDataSetChanged();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MeasurementViewHolder holder, int position) {
        if (mMeasurementItems != null) {
            final MeasurementItem measurement = mMeasurementItems.get(position);
            holder.mMeasurementTypeTextView.setText(measurement.getName().toString());
            holder.mMeasurementValueTextView.setText(measurement.getFormattedValue());
            switch (measurement.getIconResource()) {
                case MeasurementItem.ICON_RESOURCE_WEIGHT:
                    holder.mIconImageView.setImageDrawable(weightIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_WATER:
                    holder.mIconImageView.setImageDrawable(waterIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_FAT:
                    holder.mIconImageView.setImageDrawable(pizzaIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_BMI:
                    holder.mIconImageView.setImageDrawable(bmiIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_MUSCLES:
                    holder.mIconImageView.setImageDrawable(muscleIcon);
                    break;
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mMeasurementItems != null) {
            return mMeasurementItems.size();
        } else return 0;
    }

    static class MeasurementViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.measurement_item_text)
        TextView mMeasurementTypeTextView;
        @BindView(R.id.measurement_item_number)
        TextView mMeasurementValueTextView;
        @BindView(R.id.measurement_item_icon)
        ImageView mIconImageView;

        MeasurementViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
