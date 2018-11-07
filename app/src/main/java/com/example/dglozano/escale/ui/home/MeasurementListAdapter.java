package com.example.dglozano.escale.ui.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dglozano.escale.R;
import com.example.dglozano.escale.data.MeasurementItem;

import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MeasurementListAdapter extends RecyclerView.Adapter<MeasurementListAdapter.MeasurementViewHolder> {

    private List<MeasurementItem> mMeasurementItems;
    private Context mContext;

    @BindDrawable(R.drawable.home_ic_scale_colored) Drawable scaleIcon;
    @BindDrawable(R.drawable.home_ic_drop_colored) Drawable waterIcon;
    @BindDrawable(R.drawable.home_ic_pizza_slice_colored) Drawable pizzaIcon;
    @BindDrawable(R.drawable.home_ic_bone_colored) Drawable boneIcon;
    @BindDrawable(R.drawable.home_ic_bmi_colored) Drawable bmiIcon;
    @BindDrawable(R.drawable.home_ic_muscle_colored) Drawable muscleIcon;

    static class MeasurementViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.measurement_item_text) TextView mMeasurementTypeTextView;
        @BindView(R.id.measurement_item_number) TextView mMeasurementValueTextView;
        @BindView(R.id.measurement_item_icon) ImageView mIconImageView;

        MeasurementViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MeasurementListAdapter(List<MeasurementItem> measurementItemList, Context context) {
        mMeasurementItems = measurementItemList;
        mContext = context;
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

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MeasurementViewHolder holder, int position) {
        if(mMeasurementItems != null) {
            final MeasurementItem measurement = mMeasurementItems.get(position);
            holder.mMeasurementTypeTextView.setText(measurement.getName().toString());
            // FIXME: Add string resource
            holder.mMeasurementValueTextView.setText(measurement.getValue() + measurement.getUnit().toString());
            switch (measurement.getIconResource()){
                case 1:
                    holder.mIconImageView.setImageDrawable(scaleIcon);
                    break;
                case 2:
                    holder.mIconImageView.setImageDrawable(waterIcon);
                    break;
                case 3:
                    holder.mIconImageView.setImageDrawable(pizzaIcon);
                    break;
                case 4:
                    holder.mIconImageView.setImageDrawable(boneIcon);
                    break;
                case 5:
                    holder.mIconImageView.setImageDrawable(bmiIcon);
                    break;
                case 6:
                    holder.mIconImageView.setImageDrawable(muscleIcon);
                    break;
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(mMeasurementItems != null) {
            return mMeasurementItems.size();
        }
        else return 0;
    }
}
