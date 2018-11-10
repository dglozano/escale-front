package com.example.dglozano.escale.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dglozano.escale.EscaleApp;
import com.example.dglozano.escale.R;
import com.example.dglozano.escale.ble.BluetoothCommunication;
import com.example.dglozano.escale.db.EscaleDatabase;
import com.example.dglozano.escale.ui.common.BottomBarAdapter;
import com.example.dglozano.escale.ui.common.NoSwipePager;
import com.example.dglozano.escale.ui.diet.DietFragment;
import com.example.dglozano.escale.ui.home.HomeFragment;
import com.example.dglozano.escale.ui.messages.MessagesFragment;
import com.example.dglozano.escale.ui.stats.StatsFragment;
import com.example.dglozano.escale.util.PermissionHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import butterknife.BindView;
import butterknife.ButterKnife;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener {

    // Binding views with Butterknife
    @BindView(R.id.bnve)
    BottomNavigationViewEx mBnv;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fragment_viewpager)
    NoSwipePager mNoSwipePager;

    // Control Variables
    private BluetoothCommunication mBluetoothCommService;
    private boolean mBound = false;
    private boolean mConnected;
    private boolean mLoading;
    private BottomBarAdapter mPagerAdapter;
    private Badge mMessagesBadge;
    private EscaleDatabase mDatabase;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBound = true;
            BluetoothCommunication.LocalBinder localBinder = (BluetoothCommunication.LocalBinder) binder;
            mBluetoothCommService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mConnected = false;
            mLoading = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mConnected = false;
        mLoading = false;

        // Database connection and mock setup
        mDatabase = ((EscaleApp) getApplication()).getEscaleDatabase();

        // Drawer Layout setup
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        // Bottom Nav setup
        mBnv.enableAnimation(true);
        mBnv.enableShiftingMode(false);
        mBnv.enableItemShiftingMode(false);
        mBnv.setTextVisibility(false);
        mBnv.setIconSizeAt(0, 28, 28);
        mMessagesBadge = addBadgeAt(3, 0);
        mMessagesBadge.setBadgeNumber(10);

        // ViewPager setup for navigating between fragments
        mNoSwipePager.setPagingEnabled(false);
        mPagerAdapter = new BottomBarAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragments(HomeFragment.newInstance());
        mPagerAdapter.addFragments(StatsFragment.newInstance("", ""));
        mPagerAdapter.addFragments(DietFragment.newInstance("", ""));
        mPagerAdapter.addFragments(MessagesFragment.newInstance("", ""));
        mNoSwipePager.setAdapter(mPagerAdapter);

        mBnv.setupWithViewPager(mNoSwipePager);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Add notifications' number to bottomNav icon at position
    private Badge addBadgeAt(int position, int number) {
        // add badge
        return new QBadgeView(this)
                .setBadgeNumber(number)
                .setGravityOffset(22, 2, true)
                .bindTarget(mBnv.getBottomNavigationItemView(position));
    }

    @Override
    public void onMessagesRead() {
        mMessagesBadge.setBadgeNumber(0);
    }

    // Callback after PermissionHelper requested Bluetooth activation
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == PermissionHelper.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // Callback after PermissionHelper requested COARSE location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.PERMISSION_REQUEST_COARSE: {
                // si el request es cancelado el arreglo es vacio.
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.coarse_permission_message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothCommunication.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLoading = false;
        mConnected = false;
        if (mBound) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Brodcast receiver setup for receiving messages from BluetoothService
        IntentFilter filter = new IntentFilter(BluetoothCommunication.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothCommunication.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothCommunication.ACTION_DATA_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, filter);
    }

    public boolean isBound() {
        return mBound;
    }

    public BluetoothCommunication getBluetoothCommService() {
        return mBluetoothCommService;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_DATA_NOTIFICATION: received data from the device.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HomeFragment homeFragment = (HomeFragment) mPagerAdapter.getRegisteredFragment(0);
            final String action = intent.getAction();
            if (BluetoothCommunication.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mLoading = false;
                Toast.makeText(MainActivity.this, R.string.connected, Toast.LENGTH_SHORT).show();
            } else if (BluetoothCommunication.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                mLoading = false;
                Toast.makeText(MainActivity.this, R.string.disconnected, Toast.LENGTH_SHORT).show();
            } else if (BluetoothCommunication.
                    ACTION_DATA_NOTIFICATION.equals(action)) {
                // TODO Save new data.
            }
            if (homeFragment != null)
                homeFragment.refreshUi();
        }
    };

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }

    public boolean getConnected() {
        return mConnected;
    }

    public boolean getLoading() {
        return mLoading;
    }
}
