package com.dglozano.escale.ui.doctor.main.home.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.ui.doctor.main.add_goal.AddGoalActivity;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.util.Constants;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Optional;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class DoctorHomeProfileFragment extends Fragment {


    @BindView(R.id.home_doctor_user_picture)
    RoundedImageView mProfilePicture;
    @BindView(R.id.home_doctor_user_name)
    TextView mUserName;
    @BindView(R.id.home_doctor_user_email)
    TextView mUserEmail;
    @BindView(R.id.home_doctor_user_age)
    TextView mAge;
    @BindView(R.id.home_doctor_user_activity)
    TextView mPhysicalActivity;
    @BindView(R.id.home_doctor_user_height)
    TextView mHeight;
    @BindView(R.id.home_doctor_weight)
    TextView mWeight;
    @BindView(R.id.home_doctor_bmi_number)
    TextView mBmi;
    @BindView(R.id.home_doctor_fat_number)
    TextView mFat;
    @BindView(R.id.home_doctor_goal)
    TextView mGoal;
    @Inject
    DecimalFormat decimalFormat;
    @Inject
    Picasso picasso;
    @Inject
    MainActivity mMainActivity;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private Unbinder mViewUnbinder;
    private DoctorHomeProfileViewModel mViewModel;
    private MainActivityViewModel mMainActivityViewModel;


    public DoctorHomeProfileFragment() {
        // Required empty public constructor
    }

    public static DoctorHomeProfileFragment newInstance() {
        return new DoctorHomeProfileFragment();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_doctor_profile, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        mViewModel.getLoggedPatient().observe(this, this::updatePatientData);
        mViewModel.getLastBodyMeasurement().observe(this, this::updateLastMeasurement);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.doctor_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_doctor_profile_add_goal) {
            Intent intent = new Intent(getActivity(), AddGoalActivity.class);
            startActivityForResult(intent, Constants.ADD_GOAL_ACTIVITY_CODE);
        } else if (id == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return true;
    }

    private void updateLastMeasurement(Optional<BodyMeasurement> bodyMeasurement) {
        if (bodyMeasurement == null || !bodyMeasurement.isPresent()) {
            mWeight.setText("-");
            mBmi.setText("-");
            mFat.setText("-");
        } else {
            mWeight.setText(String.format("%s kg.", formatDecimal(bodyMeasurement.get().getWeight())));
            mBmi.setText(formatDecimal(bodyMeasurement.get().getBmi()));
            mFat.setText(String.format("%s %%", formatDecimal(bodyMeasurement.get().getFat())));
        }
    }

    private String formatDecimal(float value) {
        return decimalFormat.format(value).replace(",", ".");
    }

    private void updatePatientData(Patient patient) {
        if (patient != null && patient.isFullyLoaded()) {
            try {
                picasso.load(mViewModel.getProfileImageUrlOfLoggedPatient().toString())
                        .placeholder(R.color.almostWhite)
                        .noFade()
                        .error(R.drawable.ic_user_profile_image_default)
                        .into(mProfilePicture);
            } catch (MalformedURLException e) {
                Timber.e(e);
            }
            mUserName.setText(String.format("%s %s", patient.getFirstName(), patient.getLastName()));
            mUserEmail.setText(patient.getEmail());
            mAge.setText(String.format("%s años", patient.getAge()));
            mHeight.setText(String.format("%s cm.", patient.getHeightInCm()));
            mPhysicalActivity.setText(patient.getActivityString());
            if (patient.getGoalInKg() != null) {
                DecimalFormat df = new DecimalFormat("'META' ###.# 'kg'");
                mGoal.setText(df.format(patient.getGoalInKg()));
            } else {
                mGoal.setText(R.string.no_goal_set);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DoctorHomeProfileViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }
}
