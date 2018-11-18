package com.dglozano.escale.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.db.entity.User;
import com.dglozano.escale.ui.main.common.BottomBarAdapter;
import com.dglozano.escale.ui.main.common.NoSwipePager;
import com.dglozano.escale.ui.main.diet.DietFragment;
import com.dglozano.escale.ui.main.home.HomeFragment;
import com.dglozano.escale.ui.main.messages.MessagesFragment;
import com.dglozano.escale.ui.main.stats.StatsFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener,
        HasSupportFragmentInjector {

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

    @Inject
    BottomBarAdapter mPagerAdapter;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private MainActivityViewModel mViewModel;
    private Badge mMessagesBadge;
    private BleCommunicationService mBluetoothCommService;
    private boolean mBleServiceIsBound = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d( "MainActivity onServiceConnected(): Bluetooth Service is Bound.");
            mBleServiceIsBound = true;
            BleCommunicationService.LocalBinder localBinder = (BleCommunicationService.LocalBinder) binder;
            mBluetoothCommService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d( "MainActivity onServiceDisconnected(): unbinding Bluetooth Service.");
            mBleServiceIsBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainActivityViewModel.class);

        setupDrawerLayout();
        setupBottomNav();
        addFragmentsToBottomNav();

        // Updates User data in Drawer if it changes
        observeUserData();
    }

    private void setupDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void observeUserData() {
        TextView navUsername = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        TextView navEmail = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_mail);
        mViewModel.getAllUsers().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                User user = users.get(0);
                navUsername.setText(String.format("%1$s %2$s", user.getName(), user.getLastName()));
                navEmail.setText(user.getEmail());
            }
        });
    }

    private void setupBottomNav() {
        mBnv.enableAnimation(true);
        mBnv.enableShiftingMode(false);
        mBnv.enableItemShiftingMode(false);
        mBnv.setTextVisibility(false);
        mBnv.setIconSizeAt(0, 28, 28);
        mMessagesBadge = addBadgeAt(3, 0);
        mMessagesBadge.setBadgeNumber(10);
    }

    private void addFragmentsToBottomNav() {
        mNoSwipePager.setPagingEnabled(false);
        mPagerAdapter.addFragments(HomeFragment.newInstance());
        mPagerAdapter.addFragments(StatsFragment.newInstance());
        mPagerAdapter.addFragments(DietFragment.newInstance());
        mPagerAdapter.addFragments(MessagesFragment.newInstance());
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
    protected void onStart() {
        super.onStart();
        Timber.d( "MainActivity onStart(): sending intent to Bind Bluetooth Service.");
        Intent intent = new Intent(this, BleCommunicationService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.d("MainActivity onStop(): unbinding Bluetooth Service.");
        if (mBleServiceIsBound) {
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void onMessagesRead() {
        mMessagesBadge.setBadgeNumber(0);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }
}
