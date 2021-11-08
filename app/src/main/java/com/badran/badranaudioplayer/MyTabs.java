package com.badran.badranaudioplayer;

import androidx.fragment.app.Fragment;

public class MyTabs {
    Fragment fragment;
    String tabName;

    public MyTabs(String tabName, Fragment fragment) {
        this.fragment = fragment;
        this.tabName = tabName;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public String getTabName() {
        return tabName;
    }
}
