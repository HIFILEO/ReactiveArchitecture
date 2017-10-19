package com.example.reactivearchitecture.dagger;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.reactivearchitecture.viewmodel.NowPlayingViewModel;
import com.example.reactivearchitecture.viewmodel.ReactiveArchitectureViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Module for ViewModels used throughout the {@link android.app.Application}.
 */
@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(NowPlayingViewModel.class)
    abstract ViewModel bindUserViewModel(NowPlayingViewModel nowPlayingViewModel);

    //Note - The factory from Android that allows us to customize the constructor for ViewModels.
    //Otherwise ViewModels will have no constructor.
    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ReactiveArchitectureViewModelFactory factory);
}
