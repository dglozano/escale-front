package com.example.dglozano.escale;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.dglozano.escale.fragments.DietFragment;
import com.example.dglozano.escale.fragments.HomeFragment;
import com.example.dglozano.escale.fragments.MessagesFragment;
import com.example.dglozano.escale.fragments.SettingsFragment;
import com.example.dglozano.escale.fragments.StatsFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener{

    private BottomNavigationViewEx mBnv;
    private Badge mMessagesBadge;

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
        FragmentManager manager = getSupportFragmentManager();
        manager
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        mBnv.setOnNavigationItemSelectedListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    manager
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    return true;
                case R.id.action_stats:
                    manager
                            .beginTransaction()
                            .replace(R.id.fragment_container, new StatsFragment())
                            .commit();
                    return true;
                case R.id.action_diet:
                    manager
                            .beginTransaction()
                            .replace(R.id.fragment_container, new DietFragment())
                            .commit();
                    return true;
                case R.id.action_message:
                    manager
                            .beginTransaction()
                            .replace(R.id.fragment_container, new MessagesFragment())
                            .commit();
                    return true;
                case R.id.action_settings:
                    manager
                            .beginTransaction()
                            .replace(R.id.fragment_container, new SettingsFragment())
                            .commit();
                    return true;
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
                .setGravityOffset(12, 2, true)
                .bindTarget(mBnv.getBottomNavigationItemView(position));
    }

    @Override
    public void onMessagesRead() {
        mMessagesBadge.setBadgeNumber(0);
    }
}
