package com.badran.badranaudioplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class PagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<MyTabs> myTabs = new ArrayList<>();
    public PagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public void addTab(MyTabs tab) {
        myTabs.add(tab);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return myTabs.get(position).getTabName();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return myTabs.get(position).getFragment();
    }

    @Override
    public int getCount() {
        return myTabs.size();
    }
}
