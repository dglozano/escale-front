package com.dglozano.escale.ui.main.stats.list;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.ui.main.MainActivity;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StatsListAdapter extends RecyclerView.Adapter<StatsListAdapter.BodyMeasurementViewHolder> {

    @BindDrawable(R.drawable.ic_stats_arrow_down)
    Drawable arrowDown;
    @BindDrawable(R.drawable.ic_stats_arrow_up)
    Drawable arrowUp;

    private static final int UNSELECTED = -1;

    private int mExpandedPosition = UNSELECTED;
    private RecyclerView mRecyclerView;
    private List<BodyMeasurement> mBodyMeasurementList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd/MM/yyyy 'a las' H:mm 'hs.'", Locale.getDefault());
    private final DecimalFormat df = new DecimalFormat("###.##");

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Inject
    public StatsListAdapter(MainActivity mainActivity) {
        ButterKnife.bind(this, mainActivity);
    }

    @NonNull
    @Override
    public BodyMeasurementViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                   int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_group_stats, parent, false);
        return new BodyMeasurementViewHolder(itemView);
    }

    public void setItems(List<BodyMeasurement> statsList) {
        mBodyMeasurementList = statsList;
        notifyDataSetChanged();
    }

    public void addItems(List<BodyMeasurement> statsList) {
        mBodyMeasurementList.addAll(mBodyMeasurementList.size() - 1, statsList);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull BodyMeasurementViewHolder holder, int position) {
        if(mBodyMeasurementList != null ) {
            final BodyMeasurement bm = mBodyMeasurementList.get(position);
            String date = sdf.format(bm.getDate());
            holder.measurementItemWeight.setText(String.format("%s kg", df.format(bm.getWeight())));
            holder.measurementDate.setText(String.format("%s%s",
                    date.substring(0, 1).toUpperCase(),
                    date.substring(1)));
            holder.measurementItemBmi.setText(df.format(bm.getBmi()));
            holder.measurementItemFat.setText(String.format("%s %%", df.format(bm.getFat())));
            holder.measurementItemMuscle.setText(String.format("%s %%", df.format(bm.getMuscles())));
            holder.measurementItemWater.setText(String.format("%s %%", df.format(bm.getWater())));
        }

        boolean isSelected = position == mExpandedPosition;

        holder.measurementArrow.setImageDrawable(isSelected ? arrowDown : arrowUp);
        holder.measurementListExpandable.setExpanded(isSelected, false);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mBodyMeasurementList != null) {
            return mBodyMeasurementList.size();
        } else return 0;
    }

    public class BodyMeasurementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener  {

        @BindView(R.id.measurement_group_text)
        TextView measurementDate;
        @BindView(R.id.measurement_group_arrow)
        ImageView measurementArrow;
        @BindView(R.id.measurement_group_list)
        ExpandableLayout measurementListExpandable;
        @BindView(R.id.measurement_item_weight)
        TextView measurementItemWeight;
        @BindView(R.id.measurement_item_water)
        TextView measurementItemWater;
        @BindView(R.id.measurement_item_fat)
        TextView measurementItemFat;
        @BindView(R.id.measurement_item_bmi)
        TextView measurementItemBmi;
        @BindView(R.id.measurement_item_muscle)
        TextView measurementItemMuscle;

        BodyMeasurementViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            measurementListExpandable.setInterpolator(new OvershootInterpolator());
            measurementListExpandable.setOnExpansionUpdateListener(this);

            v.setOnClickListener(this);
        }
        
        @Override
        public void onClick(View view) {
            BodyMeasurementViewHolder holder = (BodyMeasurementViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mExpandedPosition);
            if (holder != null) {
                holder.measurementArrow.setImageDrawable(arrowUp);
                holder.measurementListExpandable.collapse();
            }

            int position = getAdapterPosition();
            if (position == mExpandedPosition) {
                mExpandedPosition = UNSELECTED;
            } else {
                measurementArrow.setImageDrawable(arrowDown);
                measurementListExpandable.expand();
                mExpandedPosition = position;
            }
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            Timber.d("State: %s", state);
            if (state == ExpandableLayout.State.EXPANDING) {
                mRecyclerView.smoothScrollToPosition(getAdapterPosition());
            }
        }
    }

}