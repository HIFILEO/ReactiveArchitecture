/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Inspiration:
 * https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample
 */

package com.example.reactivearchitecture.core.dagger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.example.reactivearchitecture.core.application.TestReactiveArchitectureApplication;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

/**
 * Helper class to automatically inject fragments if they implement {@link Injectable}.
 */
public class TestAppInjector {
    private TestAppInjector() {}
    public static TestApplicationComponent init(TestReactiveArchitectureApplication testReactiveArchitectureApplication) {

        //Notice the app module piece is no longer required. Unless you're creating a custom
        //ApplicationModule constructor, it's no longer needed.
        TestApplicationComponent testApplicationComponent = DaggerTestApplicationComponent.builder()
                .application(testReactiveArchitectureApplication)
                .build();
        testApplicationComponent.inject(testReactiveArchitectureApplication);

        testReactiveArchitectureApplication
                .registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        handleActivity(activity);
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {

                    }

                    @Override
                    public void onActivityResumed(Activity activity) {

                    }

                    @Override
                    public void onActivityPaused(Activity activity) {

                    }

                    @Override
                    public void onActivityStopped(Activity activity) {

                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {

                    }
                });

        return testApplicationComponent;
    }

    /**
     * Handle the injection on the activity and any fragment that implements {link @Injectable}
     * @param activity - activity to inject.
     */
    private static void handleActivity(Activity activity) {
        //Note - added because we don't always have to have fragments
        if (activity instanceof Injectable) {
            AndroidInjection.inject(activity);
        } else if (activity instanceof HasSupportFragmentInjector) {
            AndroidInjection.inject(activity);
        }
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .registerFragmentLifecycleCallbacks(
                            new FragmentManager.FragmentLifecycleCallbacks() {
                                @Override
                                public void onFragmentCreated(FragmentManager fm, Fragment f,
                                        Bundle savedInstanceState) {
                                    if (f instanceof Injectable) {
                                        AndroidSupportInjection.inject(f);
                                    }
                                }
                            }, true);
        }
    }
}
