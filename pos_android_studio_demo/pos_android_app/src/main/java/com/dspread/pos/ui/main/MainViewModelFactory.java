package com.dspread.pos.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;


public class MainViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private MainActivity activity;

    public MainViewModelFactory(Application application, MainActivity activity) {
        this.application = application;
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(application, activity);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}