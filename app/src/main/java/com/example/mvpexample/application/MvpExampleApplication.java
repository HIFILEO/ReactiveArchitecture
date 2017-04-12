/*
Copyright 2017 LEO LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.mvpexample.application;

import android.app.Application;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.example.mvpexample.dagger.ApplicationComponent;
import com.example.mvpexample.dagger.ApplicationModule;
import com.example.mvpexample.dagger.DaggerApplicationComponent;

import timber.log.Timber;

/**
 * This is the MVP application class for setting up Dagger 2 and Timber.
 */
public class MvpExampleApplication extends Application {
    private static MvpExampleApplication mvpExampleApplication;
    private ApplicationComponent component;

    public static MvpExampleApplication getInstance() {
        return mvpExampleApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MvpExampleApplication.mvpExampleApplication = this;
        setupComponent();
        setupTimber();
    }

    /**
     * Get the {@link ApplicationComponent}.
     *
     * @return The single {@link ApplicationComponent} object.
     */
    public ApplicationComponent getComponent() {
        return component;
    }

    /**
     * Setup the Timber logging tree.
     */
    void setupTimber() {
        Timber.plant(new CrashReportingTree());
    }

    /**
     * Setup the Dagger2 component graph. Must be called before {@link #onCreate()}
     */
    @VisibleForTesting
    void setupComponent() {
        if (component == null) {

            component = DaggerApplicationComponent.builder()
                    .applicationModule(getApplicationModule())
                    .build();
            component.inject(this);
        } else {
            Log.d(MvpExampleApplication.class.getSimpleName(), "setupComponent() called.  "
                    + "ApplicationComponent already set.");
        }
    }

    /**
     * Set the Dagger2 component graph.
     */
    @VisibleForTesting
    void setComponent (ApplicationComponent applicationComponent) {
        component = applicationComponent;
    }

    /**
     * Get application module.
     * @return - ApplicationModule
     */
    protected ApplicationModule getApplicationModule() {
        return new ApplicationModule(this);
    }

}
