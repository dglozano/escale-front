package com.dglozano.escale.ui.main.home;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.ui.main.MainActivityViewModel;
import com.dglozano.escale.util.Constants;
import com.dglozano.escale.util.LocationPermission;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.pawelkleczkowski.customgauge.CustomGauge;
import timber.log.Timber;

public class HomeFragment extends Fragment {

    private static final int SCAN_REQUEST_CODE = 42;

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
    @BindView(R.id.loader_text)
    TextView mLoaderText;
    @BindView(R.id.loader_webview)
    WebView mLoaderWebView;
    @BindView(R.id.weight_gauge_text)
    TextView mWeightTexView;
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

    private Unbinder mViewUnbinder;
    private HomeViewModel mHomeViewModel;
    private MainActivityViewModel mMainActivityViewModel;
    private boolean mHasClickedScan;

    private BleCommunicationService mBluetoothCommService;
    private boolean mBleServiceIsBound = false;
    private ServiceConnection mServiceConnection = new MyServiceConnection();

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    private void observeServiceLiveData() {
        Timber.d("Start observing Bluetooth Service data.");
        mBluetoothCommService.isScanningOrConnecting().observe(this, this::showLoader);
        mBluetoothCommService.getConnectionState().observe(this, this::switchConnected);
        mBluetoothCommService.getLoadingState().observe(this,
                (text) -> mLoaderText.setText(String.format("%1$s...", text)));
    }

    @NonNull
    private String formatDecimal(float weight) {
        return decimalFormat.format(weight).replace(",", ".");
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        mMainActivityViewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate().");
        mHomeViewModel = ViewModelProviders.of(this, mViewModelFactory).get(HomeViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mLoaderWebView.loadUrl("file:///android_asset/loader.html");

        mRecyclerViewMeasurements.setHasFixedSize(true);
        mRecyclerViewMeasurements.setLayoutManager(mLayoutManager);
        mRecyclerViewMeasurements.setItemAnimator(mDefaultItemAnimator);
        mRecyclerViewMeasurements.addItemDecoration(mDividerItemDecoration);
        mRecyclerViewMeasurements.setAdapter(mMeasurementListAdapter);

        mHomeViewModel.getLastBodyMeasurement().observe(this, bodyMeasurement -> {
            if(bodyMeasurement == null || !bodyMeasurement.isPresent()) {
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
            }
            mMeasurementListAdapter.addItems(MeasurementItem.getMeasurementList(
                    bodyMeasurement == null ? Optional.empty() : bodyMeasurement));
        });

        mCustomGauge.setValue(1000);
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
        Intent intent = new Intent(mMainActivity, BleCommunicationService.class);
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
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults)
                && mHasClickedScan) {
            mHasClickedScan = false;
            if (mBleServiceIsBound) {
                Timber.d("Permission granted. Starting Ble Scan...");
                mBluetoothCommService.scanBleDevices();
            } else {
                Toast.makeText(mMainActivity, "Couldn't connect. Try again...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.ble_connect_btn)
    public void onConnectButtonClicked() {
        Boolean isConnected = mBluetoothCommService.isConnected();
        if (isConnected != null && isConnected) {
            if (mBleServiceIsBound) {
                Timber.d("Pressed disconnect button. Triggering disconnection...");
                mBluetoothCommService.disposeConnection();
            } else {
                Toast.makeText(mMainActivity, "Couldn't disconnect. Try again...", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (LocationPermission.checkLocationPermissionGranted(mMainActivity)) {
                Timber.d("Clicked on connect. Permission already granted...");
                if (mBleServiceIsBound) {
                    Timber.d("BleService is Bound. Starting Ble Scan...");
                    mBluetoothCommService.scanBleDevices();
                } else {
                    Toast.makeText(mMainActivity, "Couldn't connect. Try again...", Toast.LENGTH_SHORT).show();
                }
            } else {
                Timber.d("Doesn't have Permission. Asking for it...");
                mHasClickedScan = true;
                LocationPermission.requestLocationPermissionInFragment(this);
            }
        }
    }

    private void switchConnected(String state) {
        boolean connected = state.equals(Constants.CONNECTED);
        if (connected) {
            Timber.d("Bluetooth BLE Connected. Changing Button color and text...");
            mConnectButton.setBackgroundColor(mColorPrimary);
            mConnectButton.setText(mConnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleConnectedIconDrawable);
            mAddMeasurementButton.setImageResource(R.drawable.home_ic_menu_add_weight_scale);
        } else {
            Timber.d("Bluetooth BLE Disconnected. Changing Button color and text...");
            mConnectButton.setBackgroundColor(mColorText);
            mConnectButton.setText(mDisconnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleDisonnectedIconDrawable);
            mAddMeasurementButton.setImageResource(R.drawable.home_ic_menu_add_weight_manually);
        }
    }

    private void showLoader(boolean isScanningOrConnecting) {
        if (isScanningOrConnecting) {
            Timber.d("Bluetooth BLE Scan start. Showing loader...");
            mMeasurementLayout.setVisibility(View.GONE);
            mLoaderLayout.setVisibility(View.VISIBLE);
        } else {
            Timber.d("Bluetooth BLE Scan stop. Hiding loader...");
            mLoaderLayout.setVisibility(View.GONE);
            mMeasurementLayout.setVisibility(View.VISIBLE);
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("onServiceConnected(). Bluetooth Service is Bound.");
            mBleServiceIsBound = true;
            BleCommunicationService.LocalBinder localBinder = (BleCommunicationService.LocalBinder) binder;
            mBluetoothCommService = localBinder.getService();
            observeServiceLiveData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("onServiceDisconnected(). Unbinding Bluetooth Service.");
            mBleServiceIsBound = false;
            mBluetoothCommService = null;
        }
    }
}
