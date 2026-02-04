package com.dspread.pos.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.setting.connection_settings.ConnectionSettingsFragment;
import com.dspread.pos.ui.transaction.TransactionFragment;
import com.dspread.pos_android_app.R;

/**
 * MainFragmentAdapter, used to manage fragments in ViewPager2
 * Using FragmentStateAdapter can optimize fragment creation and destruction, improving memory usage efficiency
 */
public class MainFragmentAdapter extends FragmentStateAdapter {

    // Fragment type constants
    public static final int FRAGMENT_HOME = 0;
    public static final int FRAGMENT_TRANSACTION = 1;
    public static final int FRAGMENT_SETTINGS = 2;

    // Number of fragments
    private static final int FRAGMENT_COUNT = 3;

    public MainFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create corresponding fragment based on position
        switch (position) {
            case FRAGMENT_HOME:
                return new HomeFragment();
            case FRAGMENT_TRANSACTION:
                return new TransactionFragment();
            case FRAGMENT_SETTINGS:
                return new ConnectionSettingsFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return FRAGMENT_COUNT;
    }

    /**
     * Get corresponding fragment position by navigation item ID
     */
    public int getPositionById(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                return FRAGMENT_HOME;
            case R.id.nav_transaction:
                return FRAGMENT_TRANSACTION;
            case R.id.nav_setting:
                return FRAGMENT_SETTINGS;
            default:
                return FRAGMENT_HOME;
        }
    }

    /**
     * Get corresponding navigation item ID by fragment position
     */
    public int getIdByPosition(int position) {
        switch (position) {
            case FRAGMENT_HOME:
                return R.id.nav_home;
            case FRAGMENT_TRANSACTION:
                return R.id.nav_transaction;
            case FRAGMENT_SETTINGS:
                return R.id.nav_setting;
            default:
                return R.id.nav_home;
        }
    }
}
