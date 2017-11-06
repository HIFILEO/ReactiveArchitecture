package com.example.reactivearchitecture.core.dagger;

import com.example.reactivearchitecture.core.viewcontroller.BaseActivity;
import com.example.reactivearchitecture.nowplaying.viewcontroller.NowPlayingActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Activity Components are setup here.
 */
@Module
public abstract class ActivityComponentBuilder {

    //Note - this helps us create the activity as a submodule without the need to create a
    //subcomponent which would clarify which objects are handled in that submodule. We dont'
    //need activity only objects when using viewModels.
    @ContributesAndroidInjector()
    abstract BaseActivity contributeBaseActivity();

    @ContributesAndroidInjector()
    abstract NowPlayingActivity contributeNowPlayingActivity();
}
