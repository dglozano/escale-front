package com.example.dglozano.escale;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dglozano.escale.bluetooth.BluetoothCommunication;
import com.example.dglozano.escale.fragments.DietFragment;
import com.example.dglozano.escale.fragments.HomeFragment;
import com.example.dglozano.escale.fragments.MessagesFragment;
import com.example.dglozano.escale.fragments.StatsFragment;
import com.example.dglozano.escale.utils.PermissionHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationViewEx mBnv;
    private Badge mMessagesBadge;

    // Variables de control
    private boolean mBound = false;
    private BluetoothCommunication mBluetoothCommService;

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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mBnv = findViewById(R.id.bnve);
        mBnv.enableAnimation(true);
        mBnv.enableShiftingMode(false);
        mBnv.enableItemShiftingMode(false);
        mBnv.setTextVisibility(false);
        mBnv.setIconSizeAt(0, 28, 28);
        //FIXME: como eliminar badge cuando veo ese fragment ?
        mMessagesBadge = addBadgeAt(3,0);
        mMessagesBadge.setBadgeNumber(10);

        //Replace Default Fragment
        switchFragment(HomeFragment.newInstance());

        mBnv.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    return switchFragment(HomeFragment.newInstance());
                case R.id.action_stats:
                    return switchFragment(StatsFragment.newInstance("",""));
                case R.id.action_diet:
                    return switchFragment(DietFragment.newInstance("",""));
                case R.id.action_message:
                    return switchFragment(MessagesFragment.newInstance("",""));
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Agrega numero con notificaciones (para mensajes por ejemplo)
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

    private boolean switchFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }

    // PermissionHelper solicitó habilitar Bluetooth porque estaba desactivado. Este es el callback.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == PermissionHelper.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            } else {
                if(mBound)
                    mBluetoothCommService.scanForBleDevices(getString(R.string.bf600)).thenAccept(device -> {
                        System.out.println("Got a device from remote service " + device.getAddress());
                    });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // PermissionHelper pidió permiso para COARSE, cuando elije algo vuelve acá
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.PERMISSION_REQUEST_COARSE: {
                // si el request es cancelado el arreglo es vacio.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //FIXME
                } else {
                    Toast.makeText(this, R.string.coarse_permission_message, Toast.LENGTH_SHORT).show();
                }
                return;
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
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    public boolean isBound() {
        return mBound;
    }

    public BluetoothCommunication getBluetoothCommService() {
        return mBluetoothCommService;
    }
}
