
package com.dspread.pos.ui.mifare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.posAPI.ConnectionServiceCallback;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos_android_app.databinding.FragmentMifareBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import me.goldze.mvvmhabit.utils.ToastUtils;

public class MifareFragment extends BaseFragment<FragmentMifareBinding, MifareViewModel> implements TitleProviderListener {

    private ConnectionServiceCallback connectionCallback;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private CardTypePagerAdapter pagerAdapter;
    private int currentPosition = 0;

    @Override
    public String getTitle() {
        return "Mifare Cards";
    }

    @Override
    public int initContentView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        return R.layout.fragment_mifare;
    }

    @Override
    public int initVariableId() {
        return 0;
    }

    @Override
    public void initData() {
        super.initData();
        initConnectionCallback();
    }

    private void initConnectionCallback() {
        connectionCallback = new ConnectionServiceCallback() {
            @Override
            public void onRequestNoQposDetected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device connected fail");
                });
            }

            @Override
            public void onRequestQposConnected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device connected");
                });
            }

            @Override
            public void onRequestQposDisconnected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device disconnected");
                });
            }
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager();
    }

    private void setupViewPager() {
        viewPager = binding.cardTypeViewPager;
        tabLayout = binding.cardTypeTabLayout;

        pagerAdapter = new CardTypePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("CLASSIC");
                    break;
                case 1:
                    tab.setText("ULTRALIGHT");
                    break;
                case 2:
                    tab.setText("DESFIRE");
                    break;
            }
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TRACE.i("ViewPager page selected: " + position + ", previous: " + currentPosition);
                
                if (currentPosition != position) {
                    deactivateCurrentCard(currentPosition);
                    currentPosition = position;
                }
            }
        });

        TRACE.i("ViewPager2 setup completed with 3 tabs");
    }

    private void deactivateCurrentCard(int position) {
        TRACE.i("Deactivating card for position: " + position);
        
        Fragment fragment = pagerAdapter.getFragment(position);
        if (fragment instanceof BaseCardFragment) {
            BaseCardFragment cardFragment = (BaseCardFragment) fragment;
            if (cardFragment.isPowerOn) {
                TRACE.i("Found powered-on card at position " + position + ", deactivating...");
                cardFragment.deactivateCard();
            } else {
                TRACE.i("Card at position " + position + " is not powered on");
            }
        } else {
            TRACE.i("Fragment at position " + position + " is not a BaseCardFragment");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TRACE.i("MifareFragment onDestroy: unregistering callbacks");
        POSManager.getInstance().unregisterCallbacks();
    }
}