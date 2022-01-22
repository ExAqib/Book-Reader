package com.example.bookreaders;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class pageAdapter extends FragmentPagerAdapter {
   int tabCount;

    public pageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        tabCount=behavior;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: return new poetryFragment();
            case 1: return new fictionFragment();
            case 2: return new dramaFragment();
            default:return null;

        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
