package com.example.mvpreactive.dagger;

import android.app.Activity;

import com.example.mvpreactive.viewcontroller.BaseActivity;
import com.example.mvpreactive.viewcontroller.NowPlayingActivity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

/**
 * Module for building the subcomponents for various Dagger-injected Activities in the app.
 */
@Module(subcomponents = {
        BaseActivitySubComponent.class,
        TestNowPlayingActivitySubComponent.class
    }
)
public abstract class TestActivitySubComponentBuilderModule {

    @Binds
    @IntoMap
    @ActivityKey(BaseActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindBaseActivityInjectorFactory(
            BaseActivitySubComponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(NowPlayingActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindNowPlayingInjectorFactory(
            TestNowPlayingActivitySubComponent.Builder builder);

}
