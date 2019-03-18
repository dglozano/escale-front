package com.dglozano.escale.util.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dglozano.escale.ui.main.MainActivity;
import com.dglozano.escale.util.ui.SmartFragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BottomBarAdapter extends SmartFragmentStatePagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private final MainActivity mMainActivity;

    public BottomBarAdapter(FragmentManager fragmentManager, MainActivity mainActivity) {
        super(fragmentManager);
        mMainActivity = mainActivity;
    }

    // Our custom method that populates this Adapter with Fragments
    public void addFragments(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}