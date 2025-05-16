package com.dspread.pos.common.base;

import androidx.databinding.ViewDataBinding;


import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos.ui.main.MainActivity;

import me.goldze.mvvmhabit.base.BaseViewModel;

public abstract class BaseFragment<V extends ViewDataBinding, VM extends BaseViewModel>
        extends me.goldze.mvvmhabit.base.BaseFragment<V, VM> {
    
    @Override
    public void initData() {
        super.initData();
        // 统一处理 Fragment 的初始化逻辑
        if (getActivity() instanceof MainActivity && this instanceof TitleProvider) {
            ((MainActivity) getActivity()).setToolbarTitle(((TitleProvider) this).getTitle());
        }
    }
}