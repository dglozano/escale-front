package com.dglozano.escale.ui.main;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dglozano.escale.R;
import com.dglozano.escale.ble.BF600BleService;
import com.dglozano.escale.ui.BaseActivity;
import com.dglozano.escale.ui.common.pw_change.ChangePasswordActivity;
import com.dglozano.escale.ui.drawer.profile.PatientProfileActivity;
import com.dglozano.escale.ui.login.LoginActivity;
import com.dglozano.escale.ui.main.diet.DietFragment;
import com.dglozano.escale.ui.main.home.HomeFragment;
import com.dglozano.escale.ui.main.messages.MessagesFragment;
import com.dglozano.escale.ui.main.stats.StatsFragment;
import com.dglozano.escale.util.ui.BottomBarAdapter;
import com.dglozano.escale.util.ui.Event;
import com.dglozano.escale.util.ui.NoSwipePager;
import com.dglozano.escale.web.services.FirebaseTokenSenderService;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import net.cachapa.expandablelayout.ExpandableLayout;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.net.MalformedURLException;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;
import timber.log.Timber;

import static com.dglozano.escale.ui.main.diet.all.AllDietsFragment.SHOW_PDF_CODE;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HasSupportFragmentInjector, LogoutDialog.LogoutDialogListener {

    private static final int CHANGE_PASSWORD_CODE = 123;
    public static final int ADD_MEASUREMENT_CODE = 456;

    public static final String ASK_NEW_FIREBASE_TOKEN = "ask_new_firebase_token";

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
    @BindView(R.id.main_activity_progress_bar)
    ProgressBar mMainProgressBar;
    @BindDrawable(R.drawable.ic_user_profile_image_default)
    Drawable defaultProfileImage;

    @Inject
    Picasso mPicasso;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    BottomBarAdapter mPagerAdapter;
    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private MainActivityViewModel mViewModel;
    private Badge mMessagesBadge = null;
    private Badge mNewDietsBadge = null;
    private BF600BleService mBF600BleService;
    private boolean mBleServiceIsBound = false;
    private AlertDialog activeDialog = null;
    private TextView mNavUsername;
    private TextView mNavEmail;
    private RoundedImageView mNavImageView;


    private ServiceConnection mServiceConnection = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainActivityViewModel.class);

        int openFragmentInPosition = 0;
        if (getIntent().getExtras() != null) {
            openFragmentInPosition = handleIntent();
        }
        mViewModel.setPositionOfCurrentFragment(openFragmentInPosition);

        setSupportActionBar(mToolbar);
        setActionBarTitleAccordingToFragment(openFragmentInPosition);
        setupDrawerLayout();

        onKeyboardVisibilityEvent();

        setupBottomNav();
        observeIsRefreshing();
        observeUserData();
        observeChangeDialogEvent();
        observeNumberOfUnreadMessages();
        observerFirebaseTokenUpdate();
        observeCurrentFragmentPosition();
        observeNewDietNotification();
        mViewModel.getErrorEvent().observe(this, this::showSnackbarError);
        mViewModel.getLogoutEvent().observe(this, this::onLogoutEvent);


        mViewModel.getAppBarShadowStatus().observe(this, showShadow -> {
            if (showShadow != null && !showShadow) {
                setElevationOfAppBar(0f);
            } else {
                setElevationOfAppBar(10f);
            }
        });
    }

    private void onLogoutEvent(Event<Integer> logoutEvent) {
        if (logoutEvent != null && !logoutEvent.hasBeenHandled()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra(ASK_NEW_FIREBASE_TOKEN, true);
            startActivity(intent);
            mBF600BleService.disposeConnection();
            finish();
        }
    }

    private void showSnackbarError(Event<Integer> errorEvent) {
        if (errorEvent != null && errorEvent.peekContent() != null && !errorEvent.hasBeenHandled()) {
            showSnackbarWithOkDismiss(errorEvent.handleContent());
        }
    }


    private void observeIsRefreshing() {
        mViewModel.isRefreshing().observe(this, isRefreshing -> {
            Timber.d("Refreshing status %s", isRefreshing);
            if (isRefreshing != null) {
                mMainProgressBar.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
                mNoSwipePager.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
                mExpandableBottomBar.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
                if (!isRefreshing) {
                    addFragmentsToBottomNav(mViewModel.getPositionOfCurrentFragment().getValue());
                }
            }
        });
    }

    private int handleIntent() {
        int openFragment = 0;
        String msgType = getIntent().getExtras().getString("type");
        if (msgType != null) {
            if (msgType.equals("new_message")) {
                openFragment = 3;
            } else if (msgType.equals("new_diet")) {
                openFragment = 2;
            }
        }
        return openFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.refreshData();
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            mNavigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private void observeCurrentFragmentPosition() {
        mViewModel.getPositionOfCurrentFragment().observe(this, posFragment -> {
            if (posFragment != null) {
                if (posFragment == 2) mViewModel.markNewDietAsSeen(true);
                if (posFragment == 3) mViewModel.markMessagesAsRead();
                setActionBarTitleAccordingToFragment(posFragment);
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

    private void observeNewDietNotification() {
        mViewModel.observeIfHasUnseenNewDiets().observe(this, hasNewDiet -> {
            if (mNewDietsBadge == null) {
                mNewDietsBadge = addBadgeAt(2, 0);
            }
            Timber.d("new diet observable triggered %s", hasNewDiet != null ? hasNewDiet : "null");
            if (hasNewDiet != null && hasNewDiet
                    && mViewModel.getPositionOfCurrentFragment().getValue() != null
                    && !mViewModel.getPositionOfCurrentFragment().getValue().equals(2)) {
                mNewDietsBadge.setBadgeText("!");
            } else {
                mNewDietsBadge.setBadgeNumber(0);
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
        mNavUsername = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
        mNavEmail = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_mail);
        mNavImageView = mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_picture);
        mNavImageView.setOnClickListener(view -> startProfileActivity());
    }

    private void startProfileActivity() {
        Intent intent = new Intent(this, PatientProfileActivity.class);
        startActivityForResult(intent, 1);
    }

    private void observeUserData() {


        mViewModel.getLoggedPatient().observe(this, user -> {
            if (user != null) {
                mNavUsername.setText(String.format("%1$s %2$s", user.getFirstName(), user.getLastName()));
                mNavEmail.setText(user.getEmail());

                try {
                    mPicasso.load(mViewModel.getProfileImageUrlOfLoggedPatient().toString())
                            .placeholder(R.drawable.ic_user_profile_image_default)
                            .error(R.drawable.ic_user_profile_image_default)
                            .into(mNavImageView);
                } catch (MalformedURLException e) {
                    Timber.e(e);
                }
            }
        });
    }

    private void setupBottomNav() {
        mBnv.enableAnimation(true);
        mBnv.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED); // for enableShiftingMode(false)
        mBnv.setItemHorizontalTranslationEnabled(false); // for enableItemShiftingMode(false)
        mBnv.setTextVisibility(false);
        mBnv.setIconSizeAt(0, 28, 28);
    }

    private void addFragmentsToBottomNav(int currentFragmentPosition) {
        mNoSwipePager.setPagingEnabled(false);
        mNoSwipePager.setOffscreenPageLimit(0);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startProfileActivity();
        } else if (id == R.id.nav_settings) {
            startSettingsActivity();
        } else if (id == R.id.nav_help) {

        } else if (id == R.id.nav_logout) {
            LogoutDialog.newInstance().show(getSupportFragmentManager(), "showLogoutConfirmDialog");
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivityForResult(intent, CHANGE_PASSWORD_CODE);
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
        mNotificationManager.cancelAll();
        Timber.d("onStart(). Sending intent to Bind Bluetooth Service.");
        Intent intent = new Intent(this, BF600BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_PASSWORD_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                showSnackbarWithDuration(R.string.snackbar_password_change_success_msg, Snackbar.LENGTH_SHORT);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showSnackbarWithOkDismiss(R.string.change_password_canceled_msg);
            }
        } else if (requestCode == SHOW_PDF_CODE) {
            mViewModel.setPositionOfCurrentFragment(2);
        }
    }

    public void setElevationOfAppBar(float elevation) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(elevation);
        }
        ViewCompat.setElevation(mAppBarLayout, elevation);
    }

    private void setActionBarTitleAccordingToFragment(int fragmentPosition) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            switch (fragmentPosition) {
                case 0:
                    actionBar.setTitle(R.string.home_title);
                    break;
                case 1:
                    actionBar.setTitle(R.string.stats_title);
                    break;
                case 2:
                    actionBar.setTitle(R.string.diets_title);
                    break;
                case 3:
                    actionBar.setTitle(R.string.chats_title);
                    break;
                default:
                    actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    private void observeServiceLiveData() {
        mBF600BleService.getErrorEvent().observe(this, this::showSnackbarError);
    }

    @Override
    public void onLogoutConfirmed() {
        Timber.d("Logout confirmed");
        mViewModel.logout();
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
        }
    }


}
