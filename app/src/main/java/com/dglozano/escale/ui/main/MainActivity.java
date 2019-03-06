package com.dglozano.escale.ui.main;

import android.app.Activity;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BleCommunicationService;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.common.ChangePasswordActivity;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.main.common.BottomBarAdapter;
import com.dglozano.escale.ui.main.common.NoSwipePager;
import com.dglozano.escale.ui.main.diet.DietFragment;
import com.dglozano.escale.ui.main.home.HomeFragment;
import com.dglozano.escale.ui.main.messages.MessagesFragment;
import com.dglozano.escale.ui.main.stats.StatsFragment;
import com.dglozano.escale.web.FirebaseTokenSenderService;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import net.cachapa.expandablelayout.ExpandableLayout;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

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

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HasSupportFragmentInjector {

    private static final int CHANGE_PASSWORD_CODE = 123;

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
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.expandable_bnve)
    ExpandableLayout mExpandableBottomBar;

    @Inject
    BottomBarAdapter mPagerAdapter;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    @Inject
    SharedPreferences sharedPreferences;

    private MainActivityViewModel mViewModel;
    private Badge mMessagesBadge = null;
    private BleCommunicationService mBluetoothCommService;
    private boolean mBleServiceIsBound = false;
    private AlertDialog activeDialog = null;

    private ServiceConnection mServiceConnection = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainActivityViewModel.class);

        setSupportActionBar(mToolbar);

        int currentFragmentPosition = 0;
        if (getIntent().getExtras() != null) {
            currentFragmentPosition = Integer.parseInt(getIntent().getExtras().getString("show-fragment", "0"));
        }

        setupDrawerLayout();
        setupBottomNav();
        addFragmentsToBottomNav(currentFragmentPosition);

        onKeyboardVisibilityEvent();

        observeUserData();
        observeChangeDialogEvent();
        observeNumberOfUnreadMessages();
        observerFirebaseTokenUpdate();
        observeCurrentFragmentPosition();
    }

    private void observeCurrentFragmentPosition() {
        mViewModel.getPositionOfCurrentFragment().observe(this, posFragment -> {
            if (posFragment != null) {
                switch (posFragment) {
                    case 0: // HOME
                        setElevationOfAppBar(10f);
                        break;
                    case 1: // STATS
                        setElevationOfAppBar(10f);
                        break;
                    case 2: // DIETS
                        setElevationOfAppBar(0f);
                        break;
                    case 3: // MESSAGES
                        setElevationOfAppBar(10f);
                        mViewModel.markMessagesAsRead();
                        break;
                }
            }
        });
    }

    private void observerFirebaseTokenUpdate() {
        mViewModel.getFirebaseToken().observe(this, token -> {
            Long patientId = mViewModel.getLoggedPatientId();
            if (token != null && !token.isEmpty() && !mViewModel.isFirebaseTokenSent() && patientId != -1L) {
                Timber.d("Sending token %s to server for user %s", token, patientId);
                Intent startIntent = new Intent(this, FirebaseTokenSenderService.class);
                startIntent.putExtra("token", token);
                startIntent.putExtra("patientId", patientId);
                startService(startIntent);
            }
        });
    }

    private void onKeyboardVisibilityEvent() {
        KeyboardVisibilityEvent.setEventListener(this,
                isOpen -> {
                    // some code depending on keyboard visiblity status
                    if (isOpen)
                        mExpandableBottomBar.collapse(true);
                    else
                        mExpandableBottomBar.expand(true);
                });
    }

    private void observeNumberOfUnreadMessages() {
        mViewModel.getNumberOfUnreadMessages().observe(this, unreadMessages -> {
            if (mMessagesBadge == null) {
                mMessagesBadge = addBadgeAt(3, 0);
            }
            if (unreadMessages != null) {
                mMessagesBadge.setBadgeNumber(unreadMessages);
            }
        });
    }

    private void observeChangeDialogEvent() {
        mViewModel.watchMustChangePassword().observe(this, mustChangePassEvent -> {
            if (mustChangePassEvent != null && !mustChangePassEvent.hasBeenHandled()) {
                if (mustChangePassEvent.peekContent() && activeDialog == null) {
                    activeDialog = showChangePasswordDialog();
                } else if (!mustChangePassEvent.peekContent() && activeDialog != null) {
                    activeDialog.dismiss();
                    activeDialog = null;
                }
            }
        });
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
        mViewModel.getLoggedPatient().observe(this, user -> {
            if (user != null) {
                navUsername.setText(String.format("%1$s %2$s", user.getFirstName(), user.getLastName()));
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
    }

    private void addFragmentsToBottomNav(int currentFragmentPosition) {
        mNoSwipePager.setPagingEnabled(false);
        mNoSwipePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Timber.d("New fragment position %s", position);
                mViewModel.setPositionOfCurrentFragment(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPagerAdapter.addFragments(HomeFragment.newInstance());
        mPagerAdapter.addFragments(StatsFragment.newInstance());
        mPagerAdapter.addFragments(DietFragment.newInstance());
        mPagerAdapter.addFragments(MessagesFragment.newInstance());
        mNoSwipePager.setAdapter(mPagerAdapter);
        mBnv.setupWithViewPager(mNoSwipePager);
        mNoSwipePager.setCurrentItem(currentFragmentPosition);
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

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_doctor_info) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivityForResult(intent, CHANGE_PASSWORD_CODE);
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            mViewModel.logout();
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            finish();
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

    private AlertDialog showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        return builder.setTitle(getString(R.string.change_password_title))
                .setMessage(getString(R.string.dialog_change_password))
                .setPositiveButton(R.string.change_password_title, (dialog, which) -> {
                    Intent intent = new Intent(this, ChangePasswordActivity.class);
                    intent.putExtra("forced_to_change_pass", true);
                    mViewModel.handleMustChangePasswordEvent();
                    startActivityForResult(intent, CHANGE_PASSWORD_CODE);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("onStart(). Sending intent to Bind Bluetooth Service.");
        Intent intent = new Intent(this, BleCommunicationService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mViewModel.refreshMessages();
    }

    @Override
    protected void onStop() {
        Timber.d("onStop(). Unbinding Bluetooth Service.");
        if (mBleServiceIsBound) {
            unbindService(mServiceConnection);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    @Override
    protected View getRootLayout() {
        return findViewById(android.R.id.content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHANGE_PASSWORD_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                showSnackbarWithDuration(R.string.snackbar_password_change_success_msg, Snackbar.LENGTH_SHORT);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showSnackbarWithOkDismiss(R.string.change_password_canceled_msg);
            }
        }
    }

    private void setElevationOfAppBar(float elevation) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(elevation);
        }
        ViewCompat.setElevation(mAppBarLayout, elevation);
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Timber.d("onServiceConnected(). Bluetooth Service is Bound.");
            mBleServiceIsBound = true;
            BleCommunicationService.LocalBinder localBinder = (BleCommunicationService.LocalBinder) binder;
            mBluetoothCommService = localBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Timber.d("onServiceDisconnected(). Unbinding Bluetooth Service.");
            mBleServiceIsBound = false;
        }
    }
}
