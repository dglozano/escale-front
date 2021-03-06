package com.dglozano.escale.ui.main.home;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BF600BleService;
import com.dglozano.escale.ble.CommunicationHelper;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.db.entity.Patient;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.util.AppExecutors;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.PermissionHelper;
import com.dglozano.escale.util.ui.Event;
import com.dglozano.escale.util.ui.MeasurementItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.subjects.MaybeSubject;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.pawelkleczkowski.customgauge.CustomGauge;
import timber.log.Timber;

import static com.dglozano.escale.ui.main.MainActivity.ADD_MEASUREMENT_CODE;

public class HomeFragment extends Fragment
        implements AskScaleCredentialsDialog.ScaleCredentialsDialogListener {

    @BindView(R.id.layout_measurement)
    RelativeLayout mMeasurementLayout;
    @BindView(R.id.ble_connect_btn)
    FancyButton mConnectButton;
    @BindView(R.id.recycler_view_measurements)
    RecyclerView mRecyclerViewMeasurements;
    @BindView(R.id.gauge)
    CustomGauge mCustomGauge;
    @BindView(R.id.layout_loader)
    RelativeLayout mLoaderLayout;
    @BindView(R.id.home_step_on_scale_container)
    RelativeLayout mStepOnScaleLayout;
    @BindView(R.id.loader_text)
    TextView mLoaderText;
    @BindView(R.id.loader_webview)
    WebView mLoaderWebView;
    @BindView(R.id.weight_gauge_text)
    TextView mWeightTexView;
    @BindView(R.id.weight_gauge_goal_text)
    TextView mGoalTextView;
    @BindView(R.id.bmi_number_top)
    TextView mBmiTextView;
    @BindView(R.id.fat_number_top)
    TextView mFatTextView;
    @BindView(R.id.home_last_row_image_view)
    ImageView mLastRowImageView;
    @BindView(R.id.home_last_row_text)
    TextView mLastRowText;
    @BindView(R.id.add_measurement_floating_button)
    FloatingActionButton mAddMeasurementButton;

    @BindString(R.string.connected)
    String mConnectedString;
    @BindString(R.string.disconnected)
    String mDisconnectedString;

    @BindDrawable(R.drawable.home_bluetooth_connected)
    Drawable mBleConnectedIconDrawable;
    @BindDrawable(R.drawable.home_bluetooth_disconnected)
    Drawable mBleDisonnectedIconDrawable;

    @BindColor(R.color.colorPrimary)
    int mColorPrimary;
    @BindColor(R.color.lightGray)
    int mColorText;

    @Inject
    LinearLayoutManager mLayoutManager;
    @Inject
    MeasurementListAdapter mMeasurementListAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;
    @Inject
    BodyMeasurementRepository bodyMeasurementRepository;
    @Inject
    MainActivity mMainActivity;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    DecimalFormat decimalFormat;
    @Inject
    SimpleDateFormat dateFormat;
    @Inject
    AppExecutors appExecutors;
    @Inject
    SharedPreferences sharedPreferences;

    private Unbinder mViewUnbinder;
    private HomeViewModel mHomeViewModel;
    private MainActivityViewModel mMainActivityViewModel;
    private boolean mHasClickedScan;

    private BF600BleService mBF600BleService;
    private boolean mBleServiceIsBound = false;
    private ServiceConnection mServiceConnection = new MyServiceConnection();
    private MaybeSubject<CommunicationHelper.PinIndex> mScaleCredentialsDialogSubject = null;
    private AskScaleCredentialsDialog mAskScaleCredentialsDialog = null;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    private void observeServiceLiveData() {
        Timber.d("Start observing Bluetooth Service data.");
        mBF600BleService.isScanningOrConnecting().observe(this, this::showLoader);
        mBF600BleService.getConnectionState().observe(this, this::switchConnected);
        mBF600BleService.getIsMeasurementTriggered().observe(this, this::showStepOnScale);
        mBF600BleService.showScaleCredentialsDialogEvent().observe(this, this::showScaleCredentialsDialogToLogin);
        mBF600BleService.showScaleCredentialsDialogToDeleteUserEvent().observe(this, this::showScaleCredentialsDialogToDelete);
        mBF600BleService.getBatteryLevel().observe(this, this::showBatteryLevel);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mLoaderWebView.loadUrl("file:///android_asset/loader.html");

        mRecyclerViewMeasurements.setHasFixedSize(true);
        mRecyclerViewMeasurements.setLayoutManager(mLayoutManager);
        mRecyclerViewMeasurements.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewMeasurements.addItemDecoration(mDividerItemDecoration);
        mRecyclerViewMeasurements.setAdapter(mMeasurementListAdapter);

        mHomeViewModel.getLastBodyMeasurement().observe(this, bodyMeasurement -> {
            if (bodyMeasurement == null || !bodyMeasurement.isPresent()) {
                mWeightTexView.setText("-");
                mBmiTextView.setText("-");
                mFatTextView.setText("-");
                mLastRowImageView.setVisibility(View.GONE);
                mLastRowText.setText(R.string.no_body_measurement_yet);
            } else {
                mWeightTexView.setText(formatDecimal(bodyMeasurement.get().getWeight()));
                mBmiTextView.setText(formatDecimal(bodyMeasurement.get().getBmi()));
                mFatTextView.setText(formatDecimal(bodyMeasurement.get().getFat()));
                mLastRowImageView.setVisibility(View.VISIBLE);
                mLastRowText.setText(String.format("%shs", dateFormat.format(bodyMeasurement.get().getDate())));
                appExecutors.getDiskIO().execute(() -> {
                    Optional<Float> goal = mHomeViewModel.getGoalOfLoggedPatient();
                    int startValue = sharedPreferences.getInt(Constants.GAUGE_START, 0);
                    int endValue = sharedPreferences.getInt(Constants.GAUGE_END, 100);
                    boolean hasToSetGaugeStart = sharedPreferences.getBoolean(Constants.GAUGE_HAS_TO_SET_START, false);

                    BodyMeasurement lastBodyMeasurement = mHomeViewModel.getLastBodyMeasurementBlocking().orElse(null);

                    // TODO: fix gauge according to goal type (lose or gain)

                    mMainActivity.runOnUiThread(() -> {
                        if (goal.isPresent()) {
                            if (hasToSetGaugeStart) {
                                calculateAndSetGaugeParameters(goal.get(), bodyMeasurement.get(), lastBodyMeasurement);
                            } else {
                                int gaugeValue = getGaugeValue(bodyMeasurement.get(), goal.get(), startValue, endValue);
                                mCustomGauge.setStartValue(startValue);
                                mCustomGauge.setEndValue(endValue);
                                mCustomGauge.setValue(gaugeValue);
                                mCustomGauge.invalidate();
                            }
                        } else {
                            mCustomGauge.setStartValue(0);
                            mCustomGauge.setEndValue(100);
                            mCustomGauge.setValue(100);
                        }
                    });
                });
            }
            mMeasurementListAdapter.setItems(MeasurementItem.getMeasurementList(
                    bodyMeasurement == null ? Optional.empty() : bodyMeasurement));
        });

        mHomeViewModel.getLoggedPatient().observe(this, patient -> {
            if (patient != null && patient.getGoalInKg() != null && patient.getGoalInKg() > 0f
                    && patient.getGoalDueDate() != null
                    && Calendar.getInstance().getTime().before(patient.getGoalDueDate())) {
                DecimalFormat df = new DecimalFormat("'META' ###.# 'kg'");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String goalText = String.format("%s\n%s",
                        df.format(patient.getGoalInKg()),
                        sdf.format(patient.getGoalDueDate()));
                mGoalTextView.setText(goalText);
                setupGaugeLimits(patient);
            } else {
                mGoalTextView.setText(R.string.no_goal_set);
                mCustomGauge.setStartValue(0);
                mCustomGauge.setEndValue(100);
                mCustomGauge.setValue(100);
            }
        });
    }

    private int getGaugeValue(@NonNull BodyMeasurement bodyMeasurement, @NonNull Float goal, int startValue, int endValue) {
        Timber.d("getGaugeValue");
        float difference = Math.abs(goal - bodyMeasurement.getWeight());
        int gaugeValue = Math.round(goal * 100 - difference * 100);
        gaugeValue = gaugeValue < startValue ? startValue : gaugeValue > endValue ? endValue : gaugeValue;
        return gaugeValue;
    }

    private void setupGaugeLimits(Patient patient) {
        Timber.d("setupGaugeLimits");
        appExecutors.getDiskIO().execute(() -> {
            Optional<BodyMeasurement> lastBeforeGoalStarted =
                    mHomeViewModel.getLastBodyMeasurementBeforeGoalStarted(patient.getGoalStartDate(), patient.getId());
            Optional<BodyMeasurement> firstAfterGoalStarted =
                    mHomeViewModel.getFirstBodyMeasurementAfterGoalStarted(patient.getGoalStartDate(), patient.getId());
            Optional<BodyMeasurement> initialBodyMeasurement;
            if (lastBeforeGoalStarted.isPresent()) {
                initialBodyMeasurement = lastBeforeGoalStarted;
            } else initialBodyMeasurement = firstAfterGoalStarted;

            BodyMeasurement lastBodyMeasurement = mHomeViewModel.getLastBodyMeasurementBlocking().orElse(null);

            mMainActivity.runOnUiThread(() -> {
                if (initialBodyMeasurement.isPresent()) {
                    Timber.d("initialBodyMeasurement.isPresent()");
                    calculateAndSetGaugeParameters(patient.getGoalInKg(), initialBodyMeasurement.get(), lastBodyMeasurement);
                } else {
                    Timber.d("initialBodyMeasurement is not present");
                    mCustomGauge.setStartValue(0);
                    mCustomGauge.setEndValue(Math.round(patient.getGoalInKg()) * 100);
                    mCustomGauge.setValue(0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.GAUGE_HAS_TO_SET_START, true);
                    editor.apply();
                }
                mCustomGauge.invalidate();
            });
        });
    }

    private void calculateAndSetGaugeParameters(Float goal, BodyMeasurement initialBodyMeasurement, @Nullable BodyMeasurement lastMeasurement) {
        Timber.d("calculateAndSetGaugeParameters");
        float difference = Math.abs(goal - initialBodyMeasurement.getWeight());
        int startValue = Math.round(goal * 100 - difference * 1.20f * 100);
        int endValue = Math.round(goal * 100);
        final int finalStartValue = startValue < 0 ? 0 : startValue;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.GAUGE_START, startValue);
        editor.putInt(Constants.GAUGE_END, endValue);
        editor.putBoolean(Constants.GAUGE_HAS_TO_SET_START, false);
        editor.apply();

        mCustomGauge.setStartValue(startValue);
        mCustomGauge.setEndValue(endValue);
        int gaugeValue = lastMeasurement == null ? 1000 : getGaugeValue(lastMeasurement, goal, finalStartValue, endValue);
        mCustomGauge.setValue(gaugeValue);
    }

    private void showBatteryLevel(Integer batteryLevel) {
        if (batteryLevel == null) {
            mHomeViewModel.setBatteryLevel(-1);
        } else {
            mHomeViewModel.setBatteryLevel(batteryLevel);
        }
        mMainActivity.invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        MenuItem almostEmpty = menu.findItem(R.id.home_menu_battery_almost_empty);
        MenuItem percent20 = menu.findItem(R.id.home_menu_battery_percent20);
        MenuItem percent50 = menu.findItem(R.id.home_menu_battery_percent50);
        MenuItem percent80 = menu.findItem(R.id.home_menu_battery_percent80);
        MenuItem almostFull = menu.findItem(R.id.home_menu_battery_almost_full);
        almostEmpty.setVisible(false);
        percent20.setVisible(false);
        percent50.setVisible(false);
        percent80.setVisible(false);
        almostFull.setVisible(false);
        Integer batteryLevel = mHomeViewModel.getBatteryLevel();

        if (mHomeViewModel.isScaleConnected() && batteryLevel != -1) {
            if (batteryLevel > 0 && batteryLevel < 15) {
                almostEmpty.setVisible(true);
            } else if (batteryLevel >= 15 && batteryLevel < 35) {
                percent20.setVisible(true);
            } else if (batteryLevel >= 35 && batteryLevel < 65) {
                percent50.setVisible(true);
            } else if (batteryLevel >= 65 && batteryLevel < 85) {
                percent80.setVisible(true);
            } else if (batteryLevel >= 85 && batteryLevel <= 100) {
                almostFull.setVisible(true);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home_menu_battery_almost_empty ||
                id == R.id.home_menu_battery_percent20 ||
                id == R.id.home_menu_battery_percent50 ||
                id == R.id.home_menu_battery_percent80 ||
                id == R.id.home_menu_battery_almost_full) {
            View menuItemView = mMainActivity.findViewById(id);
            String batteryLevel = String.format("%s %% Bateria", mHomeViewModel.getBatteryLevel());
            PopupMenu popupMenu = new PopupMenu(mMainActivity, menuItemView);
            popupMenu.getMenu().add(batteryLevel);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                popupMenu.dismiss();
                return true;
            });
            popupMenu.show();
        }

        return true;
    }

    private void showScaleCredentialsDialogToLogin(Event<MaybeSubject<CommunicationHelper.PinIndex>> maybeSubjectEvent) {
        showScaleCredentialsDialog(maybeSubjectEvent, false);
    }

    private void showScaleCredentialsDialogToDelete(Event<MaybeSubject<CommunicationHelper.PinIndex>> maybeSubjectEvent) {
        showScaleCredentialsDialog(maybeSubjectEvent, true);
    }

    private void showScaleCredentialsDialog(Event<MaybeSubject<CommunicationHelper.PinIndex>>
                                                    scaleCredentialsDialogEvent, boolean isDeleteDialog) {
        if (scaleCredentialsDialogEvent != null
                && !scaleCredentialsDialogEvent.hasBeenHandled()
                && mAskScaleCredentialsDialog == null) {
            Timber.d("Showing dialog credentials ...");

            mScaleCredentialsDialogSubject = scaleCredentialsDialogEvent.handleContent();
            mAskScaleCredentialsDialog = AskScaleCredentialsDialog.newInstance(isDeleteDialog);
            mAskScaleCredentialsDialog.show(HomeFragment.this.getChildFragmentManager(), "ScaleCredentialsDialog");
        }
    }

    @OnClick(R.id.add_measurement_floating_button)
    public void addMeasurementBtnOnClick(View view) {
        if (mBF600BleService.getConnectionState().getValue() != null &&
                mBF600BleService.getConnectionState().getValue().equals(Constants.CONNECTED)) {
            mBF600BleService.triggerMeasurement();
        } else {
            Intent intent = new Intent(getActivity(), AddMeasurementActivity.class);
            startActivityForResult(intent, ADD_MEASUREMENT_CODE);
        }
    }

    @NonNull
    private String formatDecimal(float weight) {
        return decimalFormat.format(weight).replace(",", ".");
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mHomeViewModel = ViewModelProviders.of(this, mViewModelFactory).get(HomeViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivityViewModel = ViewModelProviders.of(getActivity(), mViewModelFactory).get(MainActivityViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView().");
        mViewUnbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart(). Sending intent to Bind Bluetooth Service.");
        Intent intent = new Intent(mMainActivity, BF600BleService.class);
        mMainActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        Timber.d("onStop(). Unbinding Bluetooth Service.");
        if (mBleServiceIsBound) {
            mMainActivity.unbindService(mServiceConnection);
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (PermissionHelper.isRequestLocationPermissionGranted(requestCode, permissions, grantResults)
                && mHasClickedScan) {
            mHasClickedScan = false;
            if (mBleServiceIsBound) {
                Timber.d("Permission granted. Starting Ble Scan...");
                mBF600BleService.scanForBF600Scale();
            } else {
                Toast.makeText(mMainActivity, "Couldn't connect. Try again...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.ble_connect_btn)
    public void onConnectButtonClicked() {
        Boolean isConnected = mBF600BleService.isConnected();
        if (isConnected != null && isConnected) {
            if (mBleServiceIsBound) {
                Timber.d("Pressed disconnect button. Triggering disconnection...");
                mBF600BleService.disposeConnection();
            } else {
                Toast.makeText(mMainActivity, "Couldn't disconnect. Try again...", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (PermissionHelper.checkLocationPermissionGranted(mMainActivity)) {
                Timber.d("Clicked on connect. Permission already granted...");
                if (mBleServiceIsBound) {
                    Timber.d("BleService is Bound. Starting Ble Scan...");
                    mBF600BleService.scanForBF600Scale();
                } else {
                    Toast.makeText(mMainActivity, "Couldn't connect. Try again...", Toast.LENGTH_SHORT).show();
                }
            } else {
                Timber.d("Doesn't have Permission. Asking for it...");
                mHasClickedScan = true;
                PermissionHelper.requestLocationPermissionInFragment(this);
            }
        }
    }

    private void switchConnected(String state) {
        boolean connected = state.equals(Constants.CONNECTED);
        Timber.d("Bluetooth BLE State %s", state);
        mLoaderText.setText(String.format("%1$s...", state));
        mHomeViewModel.setIsScaleConnected(connected);
        if (connected) {
            mConnectButton.setBackgroundColor(mColorPrimary);
            mConnectButton.setText(mConnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleConnectedIconDrawable);
            mAddMeasurementButton.setImageResource(R.drawable.home_ic_menu_add_weight_scale);
        } else {
            if (mAskScaleCredentialsDialog != null) {
                mAskScaleCredentialsDialog.dismiss();
                mAskScaleCredentialsDialog = null;
                mScaleCredentialsDialogSubject = null;
            }
            mConnectButton.setBackgroundColor(mColorText);
            mConnectButton.setText(mDisconnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleDisonnectedIconDrawable);
            mAddMeasurementButton.setImageResource(R.drawable.home_ic_menu_add_weight_manually);
        }
        getActivity().invalidateOptionsMenu();
    }

    private void showLoader(boolean isScanningOrConnecting) {
        if (isScanningOrConnecting) {
            Timber.d("Bluetooth BLE Scan start. Showing loader...");
            if (mMeasurementLayout.getVisibility() != View.GONE)
                mMeasurementLayout.setVisibility(View.GONE);
            if (mLoaderLayout.getVisibility() != View.VISIBLE)
                mLoaderLayout.setVisibility(View.VISIBLE);
        } else {
            Timber.d("Bluetooth BLE Scan stop. Hiding loader...");
            if (mLoaderLayout.getVisibility() != View.GONE) mLoaderLayout.setVisibility(View.GONE);
            if (mMeasurementLayout.getVisibility() != View.VISIBLE)
                mMeasurementLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showStepOnScale(Boolean isMeasurementTriggered) {
        if (isMeasurementTriggered) {
            if (mStepOnScaleLayout.getVisibility() != View.VISIBLE)
                mStepOnScaleLayout.setVisibility(View.VISIBLE);
        } else {
            if (mStepOnScaleLayout.getVisibility() != View.GONE)
                mStepOnScaleLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.home_cancel_measurement)
    public void cancelMeasurement(View v) {
        mBF600BleService.stopMeasurement();
    }

    @Override
    public void onScaleCredentialsDialogSubmitCredentials() {
        mScaleCredentialsDialogSubject.onSuccess(mAskScaleCredentialsDialog.getEnteredPinIndex());
    }

    @Override
    public void onScaleCredentialsDialogCreateNewUser() {
        mScaleCredentialsDialogSubject.onComplete();
    }

    @Override
    public void onScaleCredentialsDialogCancel() {
        mAskScaleCredentialsDialog = null;
        mBF600BleService.disposeConnection();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("onServiceConnected(). Bluetooth Service is Bound.");
            mBleServiceIsBound = true;
            BF600BleService.LocalBinder localBinder = (BF600BleService.LocalBinder) binder;
            mBF600BleService = localBinder.getService();
            observeServiceLiveData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("onServiceDisconnected(). Unbinding Bluetooth Service.");
            mBleServiceIsBound = false;
            mBF600BleService = null;
        }
    }
}
