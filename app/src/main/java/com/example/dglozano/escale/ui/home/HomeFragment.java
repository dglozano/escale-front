package com.example.dglozano.escale.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.dglozano.escale.R;
import com.example.dglozano.escale.ble.BluetoothCommunication;
import com.example.dglozano.escale.data.MeasurementItem;
import com.example.dglozano.escale.data.entities.BodyMeasurement;
import com.example.dglozano.escale.ui.MainActivity;
import com.example.dglozano.escale.utils.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import mehdi.sakout.fancybuttons.FancyButton;
import pl.pawelkleczkowski.customgauge.CustomGauge;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private MainActivity mMainActivity;
    private Unbinder mViewUnbinder;

    @BindView(R.id.loader_webview) WebView mLoaderWebView;
    @BindView(R.id.layout_measurement) RelativeLayout mMeasurementLayout;
    @BindView(R.id.ble_connect_btn) FancyButton mConnectButton;
    @BindView(R.id.recycler_view_measurements) RecyclerView mRecyclerViewMeasurements;
    @BindView(R.id.gauge) CustomGauge customGauge;


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
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //FIXME
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mViewUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mLoaderWebView.loadUrl("file:///android_asset/loader.html");

        mConnectButton.setOnClickListener((View v) -> {
                if(!mMainActivity.getConnected()) {
                    if(PermissionHelper.requestBluetoothPermission(mMainActivity))
                        scanAndConnect();
                } else {
                    if(mMainActivity.isBound()) {
                        Log.d(TAG, "Clicked on disconnect");
                        mMainActivity.getBluetoothCommService().disconnect();
                    } else {
                        Log.d(TAG, "Not bound to service yet.");
                    }
                }
        });

        mRecyclerViewMeasurements.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mMainActivity);
        mRecyclerViewMeasurements.setLayoutManager(mLayoutManager);
        mRecyclerViewMeasurements.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewMeasurements.addItemDecoration(new DividerItemDecoration(
                mMainActivity,
                LinearLayoutManager.VERTICAL));

        List<MeasurementItem> mMeasurementItemList = new ArrayList<>();
        RecyclerView.Adapter mAdapterRecView = new MeasurementListAdapter(mMeasurementItemList, mMainActivity);
        mRecyclerViewMeasurements.setAdapter(mAdapterRecView);

        //TODO Borrar
        BodyMeasurement mBodyMeasurement = BodyMeasurement.createMockBodyMeasurementForUser(7);
        mMeasurementItemList.addAll(MeasurementItem.getMeasurementList(mBodyMeasurement));
        mAdapterRecView.notifyDataSetChanged();

        customGauge.setValue(1000);
    }

    public void scanAndConnect() {
        if(mMainActivity.isBound()){
            BluetoothCommunication bluetoothCommunication = mMainActivity.getBluetoothCommService();
            mMainActivity.setLoading(true);
            showLoader(true);
            bluetoothCommunication.scanForBleDevices(getString(R.string.bf600))
                    .thenAccept(bluetoothCommunication::connectGatt)
                    .exceptionally(ex -> {
                        Log.d(TAG,"Oops! We have an exception - " + ex.getMessage());
                        mMainActivity.setLoading(false);
                        mMainActivity.setConnected(false);
                        switchConnected(false);
                        showLoader(false);
                        Toast.makeText(mMainActivity, R.string.devices_not_found, Toast.LENGTH_LONG).show();
                        return null;
                    });
        }
    }

    // Cambia el color e icono del boton de conexion Bluetooth dependiendo de si se esta conectado o no
    private void switchConnected(boolean connect) {
        if(connect) {
            mConnectButton.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorPrimary));
            mConnectButton.setText("CONECTADO");
            mConnectButton.setIconResource(ContextCompat.getDrawable(mMainActivity,R.drawable.home_bluetooth_connected));
        } else {
            mConnectButton.setBackgroundColor(ContextCompat.getColor(mMainActivity, R.color.colorText));
            mConnectButton.setText("DESCONECTADO");
            mConnectButton.setIconResource(R.drawable.home_round_listview);
            mConnectButton.setIconResource(ContextCompat.getDrawable(mMainActivity,R.drawable.home_bluetooth_disconnected));
        }
    }

    // Switch entre el loader y la vista normal
    private void showLoader(boolean show) {
        if(show){
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
        Log.d(TAG, "OnResume Fragment Home");
        refreshUi();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored Fragment Home");
        refreshUi();
    }

    public void refreshUi() {
        switchConnected(mMainActivity.getConnected());
        showLoader(mMainActivity.getLoading());
        // TODO refrescar datos recibidos.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewUnbinder.unbind();
    }
}
