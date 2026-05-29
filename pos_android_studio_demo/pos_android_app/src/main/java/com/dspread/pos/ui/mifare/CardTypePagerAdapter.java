package com.dspread.pos.ui.mifare;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

public class CardTypePagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public CardTypePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = ClassicFragment.newInstance();
                break;
            case 1:
                fragment = UltralightFragment.newInstance();
                break;
            case 2:
                fragment = DesfireFragment.newInstance();
                break;
            default:
                fragment = ClassicFragment.newInstance();
                break;
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }

    public void removeFragment(int position) {
        fragmentMap.remove(position);
    }
}
