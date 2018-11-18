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
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.repository.BodyMeasurementRepository;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.LocationPermission;

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

    @BindView(R.id.loader_webview)
    WebView mLoaderWebView;
    @BindView(R.id.layout_measurement)
    RelativeLayout mMeasurementLayout;
    @BindView(R.id.ble_connect_btn)
    FancyButton mConnectButton;
    @BindView(R.id.recycler_view_measurements)
    RecyclerView mRecyclerViewMeasurements;
    @BindView(R.id.gauge)
    CustomGauge mCustomGauge;

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
    @BindColor(R.color.colorText)
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

    private Unbinder mViewUnbinder;
    private HomeViewModel mHomeViewModel;
    private boolean mHasClickedScan;

    private BleCommunicationService mBluetoothCommService;
    private boolean mBleServiceIsBound = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("HomeFragment onServiceConnected(): Bluetooth Service is Bound.");
            mBleServiceIsBound = true;
            BleCommunicationService.LocalBinder localBinder = (BleCommunicationService.LocalBinder) binder;
            mBluetoothCommService = localBinder.getService();
            observeServiceLiveData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("HomeFragment onServiceDisconnected(): unbinding Bluetooth Service.");
            mBleServiceIsBound = false;
        }
    };

    private void observeServiceLiveData() {
        mBluetoothCommService.isScanningOrConnecting().observe(this, this::showLoader);
        mBluetoothCommService.isConnectedToScale().observe(this, this::switchConnected);
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
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        //TODO Borrar
        BodyMeasurement mBodyMeasurement = BodyMeasurement.createMockBodyMeasurementForUser(7);
        mMeasurementListAdapter.addItems(MeasurementItem.getMeasurementList(mBodyMeasurement));

        mCustomGauge.setValue(1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView");
        mViewUnbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("HomeFragment onStart(): sending intent to Bind Bluetooth Service.");
        Intent intent = new Intent(mMainActivity, BleCommunicationService.class);
        mMainActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("HomeFragment onStop(): unbinding Bluetooth Service.");
        if (mBleServiceIsBound) {
            mMainActivity.unbindService(mServiceConnection);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults)
                && mHasClickedScan) {
            mHasClickedScan = false;
            if (mBleServiceIsBound) {
                mBluetoothCommService.scanBleDevices();
            }
        }
    }

    @OnClick(R.id.ble_connect_btn)
    public void onConnectButtonClicked() {
        Boolean isConnected = mBluetoothCommService.isConnectedToScale().getValue();
        if (isConnected != null && isConnected) {
            if (mBleServiceIsBound) {
                mBluetoothCommService.disposeConnection();
            }
        } else {
            if (LocationPermission.checkLocationPermissionGranted(mMainActivity)) {
                if (mBleServiceIsBound) {
                    mBluetoothCommService.scanBleDevices();
                }
            } else {
                mHasClickedScan = true;
                LocationPermission.requestLocationPermissionInFragment(this);
            }
        }
    }

    private void switchConnected(boolean connected) {
        if (connected) {
            mConnectButton.setBackgroundColor(mColorPrimary);
            mConnectButton.setText(mConnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleConnectedIconDrawable);
        } else {
            mConnectButton.setBackgroundColor(mColorText);
            mConnectButton.setText(mDisconnectedString.toUpperCase());
            mConnectButton.setIconResource(mBleDisonnectedIconDrawable);
        }
    }

    private void showLoader(boolean isScanningOrConnecting) {
        if (isScanningOrConnecting) {
            Timber.d("Bluetooth BLE Scan start. Showing loader.");
            mMeasurementLayout.setVisibility(View.GONE);
            mLoaderWebView.setVisibility(View.VISIBLE);
        } else {
            Timber.d("Bluetooth BLE Scan stop. Hiding loader.");
            mLoaderWebView.setVisibility(View.GONE);
            mMeasurementLayout.setVisibility(View.VISIBLE);
        }
    }
}
