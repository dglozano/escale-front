package com.dglozano.escale.ui.doctor.main;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.db.entity.PatientInfo;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.doctor.main.add_patient.AddPatientActivity;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.main.LogoutDialog;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.ui.Event;
import com.dglozano.escale.web.services.FirebaseTokenSenderService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;
import timber.log.Timber;

public class DoctorMainActivity extends BaseActivity
        implements LogoutDialog.LogoutDialogListener, SearchView.OnQueryTextListener {

    private static final int ADD_PATIENT_CODE = 234;

    @BindView(R.id.doctor_main_activity_progress_bar_layout)
    RelativeLayout mDoctorMainProgressBar;
    @BindView(R.id.doctor_patients_rv_list)
    RecyclerView mPatientsRecyclerList;
    @BindView(R.id.no_patients_layout)
    RelativeLayout mNoPatientsLayout;
    @BindView(R.id.doctor_add_patient_floating_btn)
    FloatingActionButton mAddPatientButton;

    @Inject
    PatientsListAdapter mPatientsInfoAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private DoctorMainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DoctorMainActivityViewModel.class);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.drawer_ic_menu_logout);

        mViewModel.getLogoutEvent().observe(this, this::onLogoutEvent);
        mViewModel.getFirebaseToken().observe(this, this::onFirebaseTokenUpdate);
        mViewModel.getLoadingStatus().observe(this, this::onLoadingStatusUpdate);

        setupRecyclerList();
    }

    private void setupRecyclerList() {
        mPatientsRecyclerList.setHasFixedSize(true);
        mPatientsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mPatientsRecyclerList.setItemAnimator(mDefaultItemAnimator);
        mPatientsRecyclerList.addItemDecoration(mDividerItemDecoration);
        mPatientsInfoAdapter.setPatientClickListener(patientInfo -> {
            mViewModel.setPatientId(patientInfo.getPatientId());
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.IS_DOCTOR_VIEW_INTENT_EXTRA, true);
            startActivityForResult(intent, Constants.MAIN_ACTIVITY_STARTED_BY_DOCTOR_CODE);
        });
        mPatientsRecyclerList.setAdapter(mPatientsInfoAdapter);
        mViewModel.getAllPatientInfoForLoggedDoctor().observe(this, patientInfos -> {
            if (patientInfos != null && !patientInfos.isEmpty()) {
                mNoPatientsLayout.setVisibility(View.GONE);
                mPatientsRecyclerList.setVisibility(View.VISIBLE);
                mPatientsInfoAdapter.replaceAll(patientInfos);
            } else {
                mPatientsRecyclerList.setVisibility(View.GONE);
                mNoPatientsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onLoadingStatusUpdate(Boolean isLoading) {
        if (isLoading != null && !isLoading) {
            mDoctorMainProgressBar.setVisibility(View.GONE);
            mAddPatientButton.setVisibility(View.VISIBLE);
        } else {
            mDoctorMainProgressBar.setVisibility(View.VISIBLE);
            mAddPatientButton.setVisibility(View.GONE);
        }
    }

    private void onFirebaseTokenUpdate(String token) {
        Long doctorId = mViewModel.getLoggedDoctorId();
        if (token != null && !token.isEmpty() && !mViewModel.isFirebaseTokenSent() && doctorId != -1L) {
            Timber.d("Sending token %s to server for user %s", token, doctorId);
            Intent startIntent = new Intent(this, FirebaseTokenSenderService.class);
            startIntent.putExtra("token", token);
            startIntent.putExtra("userId", doctorId);
            startService(startIntent);
        }
    }

    @OnClick(R.id.doctor_add_patient_floating_btn)
    public void onClick(View v) {
        Intent intent = new Intent(this, AddPatientActivity.class);
        startActivityForResult(intent, ADD_PATIENT_CODE);
    }

    private void onLogoutEvent(Event<Integer> logoutEvent) {
        if (logoutEvent != null && !logoutEvent.hasBeenHandled()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(Constants.ASK_NEW_FIREBASE_TOKEN, true);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_doctor_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (mViewModel.getAllPatientInfoForLoggedDoctor().getValue() != null) {
            final List<PatientInfo> filteredModelList = filter(mViewModel.getAllPatientInfoForLoggedDoctor().getValue(), query);
            mPatientsInfoAdapter.replaceAll(filteredModelList);
            mPatientsRecyclerList.scrollToPosition(0);
        }
        return true;
    }

    private static List<PatientInfo> filter(List<PatientInfo> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<PatientInfo> filteredModelList = new ArrayList<>();
        for (PatientInfo model : models) {
            final String text = model.getFullName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PATIENT_CODE && resultCode == Activity.RESULT_OK) {
            showSnackbarWithDuration(R.string.patient_added_successfully, Snackbar.LENGTH_SHORT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.refreshData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNotificationManager.cancelAll();
    }

    @Override
    public void onLogoutConfirmed() {
        Timber.d("Logout doctor confirmed");
        mViewModel.logout();
    }

    @Override
    public boolean onSupportNavigateUp() {
        LogoutDialog.newInstance().show(getSupportFragmentManager(), "showLogoutConfirmDialog");
        return false;
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }
}
