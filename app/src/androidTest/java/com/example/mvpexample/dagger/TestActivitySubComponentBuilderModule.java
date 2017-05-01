package com.example.mvpexample.dagger;

import android.app.Activity;

import com.example.mvpexample.util.BaseTest;
import com.example.mvpexample.viewcontroller.BaseActivity;
import com.example.mvpexample.viewcontroller.NowPlayingActivity;
import com.example.mvpexample.viewcontroller.NowPlayingActivityTest;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

/**
 * Module for building the subcomponents for various Dagger-injected Activities in the app.
 */
@Module(subcomponents = {
        BaseActivitySubComponent.class,
        TestNowPlayingActivitySubComponent.class,
        NowPlayingActivityTestSubComponent.class
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

    @Binds
    @IntoMap
    @ClassKey(NowPlayingActivityTest.class)
    abstract AndroidInjector.Factory<? extends BaseTest> bindNowPlayingTestInjectorFactory(
            NowPlayingActivityTestSubComponent.Builder builder);
}
