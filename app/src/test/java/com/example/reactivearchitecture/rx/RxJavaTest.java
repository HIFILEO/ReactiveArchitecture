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
package com.example.reactivearchitecture.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.categories.UnitTest;

import org.junit.Before;
import org.junit.experimental.categories.Category;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.TestScheduler;

/**
 * Base class for all tests that use RxJava/RxAndroid.
 */
@Category(UnitTest.class)
public abstract class RxJavaTest {
    @VisibleForTesting
    protected TestScheduler testScheduler;

    @Before
    @CallSuper
    public void setUp() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
        testScheduler = new TestScheduler();

        setupJavaSchedulers();
        setupAndroidSchedulers();
    }

    /**
     * Setup the Rx java schedulers.
     */
    private void setupJavaSchedulers() {
        RxJavaPlugins.setIoSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
                return testScheduler;
            }
        });

        RxJavaPlugins.setComputationSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
                return testScheduler;
            }
        });

        RxJavaPlugins.setNewThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
                return testScheduler;
            }
        });

        RxJavaPlugins.setSingleSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
                return testScheduler;
            }
        });
    }

    /**
     * Setup the Rx Android schedulers.
     */
    private void setupAndroidSchedulers() {
        RxAndroidPlugins.onMainThreadScheduler(testScheduler);

        RxAndroidPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Scheduler scheduler) throws Exception {
                return testScheduler;
            }
        });

        RxAndroidPlugins.setInitMainThreadSchedulerHandler(new Function<Callable<Scheduler>, Scheduler>() {
            @Override
            public Scheduler apply(@NonNull Callable<Scheduler> schedulerCallable) throws Exception {
                return testScheduler;
            }
        });

        RxAndroidPlugins.initMainThreadScheduler(new Callable<Scheduler>() {
            @Override
            public Scheduler call() throws Exception {
                return testScheduler;
            }
        });

    }
}
