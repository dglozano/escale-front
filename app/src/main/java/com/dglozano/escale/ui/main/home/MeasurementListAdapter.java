package com.dglozano.escale.ui.main.home;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ui.main.MainActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MeasurementListAdapter extends RecyclerView.Adapter<MeasurementListAdapter.MeasurementViewHolder> {

    @BindDrawable(R.drawable.home_ic_scale_colored)
    Drawable scaleIcon;
    @BindDrawable(R.drawable.home_ic_drop_colored)
    Drawable waterIcon;
    @BindDrawable(R.drawable.home_ic_pizza_slice_colored)
    Drawable pizzaIcon;
    @BindDrawable(R.drawable.home_ic_bone_colored)
    Drawable boneIcon;
    @BindDrawable(R.drawable.home_ic_bmi_colored)
    Drawable bmiIcon;
    @BindDrawable(R.drawable.home_ic_muscle_colored)
    Drawable muscleIcon;
    @BindString(R.string.measurement_item_holder_value)
    String measurementItemHolderValueString;
    @BindString(R.string.decimal_format)
    String decimalFormat;

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
                .inflate(R.layout.measurement_row_item, parent, false);

        return new MeasurementViewHolder(itemView);
    }

    public void addItems(List<MeasurementItem> measurementItemList) {
        mMeasurementItems.addAll(measurementItemList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mMeasurementItems.clear();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MeasurementViewHolder holder, int position) {
        if (mMeasurementItems != null) {
            final MeasurementItem measurement = mMeasurementItems.get(position);
            holder.mMeasurementTypeTextView.setText(measurement.getName().toString());
            holder.mMeasurementValueTextView.setText(String.format(measurementItemHolderValueString,
                    getFormattedValue(measurement.getValue()),
                    measurement.getUnit().toString()));
            switch (measurement.getIconResource()) {
                case MeasurementItem.ICON_RESOURCE_WEIGHT:
                    holder.mIconImageView.setImageDrawable(scaleIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_WATER:
                    holder.mIconImageView.setImageDrawable(waterIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_FAT:
                    holder.mIconImageView.setImageDrawable(pizzaIcon);
                    break;
                case MeasurementItem.ICON_RESOURCE_BONES:
                    holder.mIconImageView.setImageDrawable(boneIcon);
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

    private String getFormattedValue(float v) {
        DecimalFormat df = new DecimalFormat(decimalFormat);
        return df.format(v);
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
