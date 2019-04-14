package com.dglozano.escale.ui.doctor.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.PatientInfo;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PatientsListAdapter extends RecyclerView.Adapter<PatientsListAdapter.PatientViewHolder> {

    @BindDrawable(R.drawable.ic_user_profile_image_default)
    Drawable defaultProfileImage;
    @BindDrawable(R.drawable.ic_no_alert)
    Drawable noAlertDrawable;
    @BindDrawable(R.drawable.ic_alert)
    Drawable alertDrawable;
    @BindDrawable(R.drawable.ic_message)
    Drawable messageDrawable;
    @BindDrawable(R.drawable.ic_no_message)
    Drawable noMessageDrawable;
    @BindColor(R.color.almostWhite)
    int almostWhite;
    @BindColor(R.color.colorAccentVeryLight)
    int accentVeryLight;
    @BindString(R.string.number_of_new_patient_messages)
    String newMessagesString;
    @BindString(R.string.number_of_new_patient_alerts)
    String newAlertsString;

    @Inject
    Picasso mPicasso;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private List<PatientInfo> mPatientsInfoList;
    private PatientClickListener mClickListener;
    private DoctorMainActivityViewModel mDoctorMainActivityViewModel;
    private Context mContext;

    @Inject
    public PatientsListAdapter(DoctorMainActivity doctorMainActivity,
                               DoctorMainActivityViewModel doctorMainActivityViewModel) {
        this.mContext = doctorMainActivity;
        this.mDoctorMainActivityViewModel = doctorMainActivityViewModel;
        ButterKnife.bind(this, doctorMainActivity);
    }

    void setPatientClickListener(PatientClickListener patientClickListener) {
        this.mClickListener = patientClickListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_patient, parent, false);
        return new PatientViewHolder(itemView, mClickListener);
    }

    public void setItems(List<PatientInfo> patientInfoList) {
        mPatientsInfoList = patientInfoList;
        notifyDataSetChanged();
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        if (mPatientsInfoList != null) {
            final PatientInfo patientInfo = mPatientsInfoList.get(position);
            holder.mPatientFullName.setText(patientInfo.getFullName());
            DecimalFormat df = new DecimalFormat("###.#");
            holder.mPatientWeight.setText(patientInfo.getLastWeight() == null ? "-" : df.format(patientInfo.getLastWeight()));
            holder.itemView.setBackgroundColor(patientInfo.getAlerts() > 0 || patientInfo.getMessages() > 0 ? accentVeryLight : almostWhite);
            holder.mAlertIcon.setImageDrawable(patientInfo.getAlerts() > 0 ? alertDrawable : noAlertDrawable);
            holder.mMessagesIcon.setImageDrawable(patientInfo.getMessages() > 0 ? messageDrawable : noMessageDrawable);

            holder.mAlertTextView.setText(MessageFormat.format(newAlertsString, patientInfo.getAlerts()));
            holder.mMessagesTextView.setText(MessageFormat.format(newMessagesString, patientInfo.getMessages()));

            try {
                mPicasso.load(mDoctorMainActivityViewModel
                        .getProfileImageUrlPatient(patientInfo.getPatientId()).toString())
                        .placeholder(R.drawable.ic_user_profile_image_default)
                        .error(R.drawable.ic_user_profile_image_default)
                        .into(holder.mProfilePicture);
            } catch (MalformedURLException e) {
                Timber.e(e);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mPatientsInfoList != null) {
            return mPatientsInfoList.size();
        } else return 0;
    }

    public interface PatientClickListener {

        void onClick(PatientInfo patientInfo);
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.row_item_patient_picture)
        RoundedImageView mProfilePicture;
        @BindView(R.id.row_item_patient_fullname)
        TextView mPatientFullName;
        @BindView(R.id.row_item_patient_weight)
        TextView mPatientWeight;
        @BindView(R.id.row_item_patient_alert_icon)
        ImageView mAlertIcon;
        @BindView(R.id.row_item_patient_alerts_textview)
        TextView mAlertTextView;
        @BindView(R.id.row_item_patient_messages_icon)
        ImageView mMessagesIcon;
        @BindView(R.id.row_item_patient_messages_textview)
        TextView mMessagesTextView;

        PatientClickListener mListener;

        PatientViewHolder(View v, PatientClickListener listener) {
            super(v);
            ButterKnife.bind(this, v);
            mListener = listener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RecyclerView rv = (RecyclerView) v.getParent();
            int itemPosition = rv.getChildLayoutPosition(v);
            PatientInfo patient = mPatientsInfoList.get(itemPosition);
            mListener.onClick(patient);
        }
    }
}
