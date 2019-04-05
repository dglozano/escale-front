package com.dglozano.escale.util.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dglozano.escale.ui.main.MainActivity;

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