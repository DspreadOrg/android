package com.dspread.pos.common.base;

import androidx.databinding.ViewDataBinding;


import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.ui.main.MainActivity;

import me.goldze.mvvmhabit.base.BaseViewModel;

public abstract class BaseFragment<V extends ViewDataBinding, VM extends BaseViewModel>
        extends me.goldze.mvvmhabit.base.BaseFragment<V, VM> {
    
    @Override
    public void initData() {
        super.initData();
        // Unified initialization logic for handling fragment
        if (getActivity() instanceof MainActivity && this instanceof TitleProviderListener) {
            ((MainActivity) getActivity()).setToolbarTitle(((TitleProviderListener) this).getTitle());
        }
    }
}