package com.dglozano.escale.ui.main.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dglozano.escale.ble.BluetoothCommunication;
import com.dglozano.escale.db.MeasurementItem;
import com.dglozano.escale.db.entity.BodyMeasurement;
import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.PermissionHelper;
import com.dglozano.escale.R;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.pawelkleczkowski.customgauge.CustomGauge;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
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
    @BindString(R.string.bf600)
    String mScaleBleName;

    @Inject
    LinearLayoutManager mLayoutManager;
    @Inject
    MeasurementListAdapter mMeasurementListAdapter;
    @Inject
    DefaultItemAnimator mDefaultItemAnimator;
    @Inject
    DividerItemDecoration mDividerItemDecoration;

    private Unbinder mViewUnbinder;
    private MainActivity mMainActivity;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);
        mMainActivity = (MainActivity) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mLoaderWebView.loadUrl("file:///android_asset/loader.html");

        mConnectButton.setOnClickListener((View v) -> {
            if (!mMainActivity.getConnected()) {
                if (PermissionHelper.requestBluetoothPermission(mMainActivity))
                    scanAndConnect();
            } else {
                if (mMainActivity.isBound()) {
                    Timber.d("Clicked on disconnect");
                    mMainActivity.getBluetoothCommService().disconnect();
                } else {
                    Timber.d("Not bound to service yet.");
                }
            }
        });

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

    public void scanAndConnect() {
        if (mMainActivity.isBound()) {
            BluetoothCommunication bluetoothCommunication = mMainActivity.getBluetoothCommService();
            mMainActivity.setLoading(true);
            showLoader(true);
            bluetoothCommunication.scanForBleDevices(mScaleBleName)
                    .thenAccept(bluetoothCommunication::connectGatt)
                    .exceptionally(ex -> {
                        Timber.e(ex, "Ups, we had an exception while connection to %1$s", mScaleBleName);
                        mMainActivity.setLoading(false);
                        mMainActivity.setConnected(false);
                        switchConnected(false);
                        showLoader(false);
                        Toast.makeText(mMainActivity, R.string.devices_not_found, Toast.LENGTH_LONG).show();
                        return null;
                    });
        }
    }

    // Change Bluetooth Button color
    private void switchConnected(boolean connect) {
        if (connect) {
            mConnectButton.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorPrimary));
            mConnectButton.setText("CONECTADO");
            mConnectButton.setIconResource(ContextCompat.getDrawable(mMainActivity, R.drawable.home_bluetooth_connected));
        } else {
            mConnectButton.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorText));
            mConnectButton.setText("DESCONECTADO");
            mConnectButton.setIconResource(R.drawable.home_round_listview);
            mConnectButton.setIconResource(ContextCompat.getDrawable(mMainActivity, R.drawable.home_bluetooth_disconnected));
        }
    }

    // Switch between loader and normal view
    private void showLoader(boolean show) {
        if (show) {
            mMeasurementLayout.setVisibility(View.GONE);
            mLoaderWebView.setVisibility(View.VISIBLE);
        } else {
            mLoaderWebView.setVisibility(View.GONE);
            mMeasurementLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("OnResume");
        refreshUi();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Timber.d("onViewStateRestored");
        refreshUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.d("onDestroyView");
        mViewUnbinder.unbind();
    }

    public void refreshUi() {
        switchConnected(mMainActivity.getConnected());
        showLoader(mMainActivity.getLoading());
        // TODO refrescar datos recibidos.
    }
}
