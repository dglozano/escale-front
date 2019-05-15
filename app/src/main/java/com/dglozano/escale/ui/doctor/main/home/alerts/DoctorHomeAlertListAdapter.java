package com.dglozano.escale.ui.doctor.main.home.alerts;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.Alert;
import com.dglozano.escale.ui.main.MainActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DoctorHomeAlertListAdapter extends RecyclerView.Adapter<DoctorHomeAlertListAdapter.AlertViewHolder> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy H:mm 'hs.'", Locale.getDefault());
    private final DecimalFormat df = new DecimalFormat("###.##");
    @BindDrawable(R.drawable.ic_forecast_alert)
    Drawable forecastIcon;
    @BindDrawable(R.drawable.ic_goal_fail_alert)
    Drawable goalFailIcon;
    @BindDrawable(R.drawable.ic_goal_success_alert)
    Drawable goalSuccessIcon;
    @BindDrawable(R.drawable.ic_no_measurement_alert)
    Drawable noMeasurementIcon;
    @BindDrawable(R.drawable.ic_manual_mesaurement_icon)
    Drawable manualMeasurementIcon;
    @BindDrawable(R.drawable.ic_alert)
    Drawable defaultAlert;
    @BindColor(R.color.colorAccentVeryLight)
    int lightAccent;
    @BindColor(android.R.color.white)
    int white;
    private AlertClickListener mClickListener;
    private RecyclerView mRecyclerView;
    private List<Alert> mAlertList;

    @Inject
    public DoctorHomeAlertListAdapter(MainActivity mainActivity) {
        ButterKnife.bind(this, mainActivity);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    public void setAlertClickListener(AlertClickListener alertClickListener) {
        this.mClickListener = alertClickListener;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_alert, parent, false);
        return new AlertViewHolder(itemView, mClickListener);
    }

    public void setItems(List<Alert> statsList) {
        mAlertList = statsList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        if (mAlertList != null) {
            final Alert alert = mAlertList.get(position);
            String alertDate = sdf.format(alert.getDateCreated() == null ? Calendar.getInstance().getTime() : alert.getDateCreated());
            holder.alertText.setText(String.format("%s (%s)", alert.getMessage() == null ? "" : alert.getMessage(), alertDate));
            if (alert.getAlertType() == null)
                holder.alertImg.setImageDrawable(defaultAlert);
            else switch (alert.getAlertType()) {
                case FORECAST_PREDICTS_GOAL_WILL_FAIL:
                    holder.alertImg.setImageDrawable(forecastIcon);
                    break;
                case GOAL_FAILED:
                    holder.alertImg.setImageDrawable(goalFailIcon);
                    break;
                case NO_RECENT_MEASUREMENT:
                    holder.alertImg.setImageDrawable(noMeasurementIcon);
                    break;
                case MANUAL_MEASUREMENT:
                    holder.alertImg.setImageDrawable(manualMeasurementIcon);
                    break;
                case GOAL_SUCCESS:
                default:
                    holder.alertImg.setImageDrawable(goalSuccessIcon);
                    break;
            }
            if (alert.isSeenByDoctor()) {
                holder.alertText.setTypeface(Typeface.DEFAULT);
                animateBackgroundColorChange(holder, true);
            } else {
                holder.alertText.setTypeface(holder.alertText.getTypeface(), Typeface.BOLD);
                animateBackgroundColorChange(holder, false);
            }
        }
    }

    private void animateBackgroundColorChange(@NonNull AlertViewHolder holder, boolean seen) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                ((ColorDrawable) holder.itemView.getBackground()).getColor(), seen ? white : lightAccent);
        colorAnimation.setDuration(200); // milliseconds
        colorAnimation.addUpdateListener(animator ->
                holder.itemView.setBackgroundColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mAlertList != null) {
            return mAlertList.size();
        } else return 0;
    }

    public interface AlertClickListener {

        void onClick(Alert alert);
    }

    public class AlertViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.alert_row_text)
        TextView alertText;
        @BindView(R.id.alert_row_img)
        ImageView alertImg;

        AlertClickListener mListener;

        AlertViewHolder(View v, AlertClickListener alertClickListener) {
            super(v);
            ButterKnife.bind(this, v);
            mListener = alertClickListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RecyclerView rv = (RecyclerView) v.getParent();
            int itemPosition = rv.getChildLayoutPosition(v);
            Alert alert = mAlertList.get(itemPosition);
            mListener.onClick(alert);
        }
    }

}