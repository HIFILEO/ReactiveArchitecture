package com.example.mvpexample.rx;

import android.support.annotation.CallSuper;
import android.support.annotation.VisibleForTesting;

import com.example.mvpexample.categories.UnitTest;

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
