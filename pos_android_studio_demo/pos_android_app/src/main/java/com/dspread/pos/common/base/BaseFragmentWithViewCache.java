package com.dspread.pos.common.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import me.goldze.mvvmhabit.base.BaseFragment;
import me.goldze.mvvmhabit.base.BaseViewModel;

/**
 * BaseFragment with view caching, used to optimize fragment switching smoothness
 * When the fragment is hidden, save its view, and when the fragment is shown again, directly use the saved view instead of recreating it
 */
public abstract class BaseFragmentWithViewCache<V extends ViewDataBinding, VM extends BaseViewModel> extends BaseFragment<V, VM> {
    
    // View cache
    private V mViewDataBindingCache;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // If view cache is not null, directly return the view cache
        if (mViewDataBindingCache != null) {
            return mViewDataBindingCache.getRoot();
        }
        
        // Otherwise create a new view
        mViewDataBindingCache = DataBindingUtil.inflate(inflater, initContentView(inflater, container, savedInstanceState), container, false);
        binding = mViewDataBindingCache;
        return binding.getRoot();
    }
    
    @Override
    public void onDestroyView() {
        // Don't call super.onDestroyView() to avoid view destruction
        // super.onDestroyView();
        
        // Keep view cache, don't destroy view
        // Note: This method will increase memory usage, but it can improve fragment switching smoothness
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        
        // Clear view cache when fragment is completely detached
        if (mViewDataBindingCache != null) {
            mViewDataBindingCache.unbind();
            mViewDataBindingCache = null;
        }
    }
}
