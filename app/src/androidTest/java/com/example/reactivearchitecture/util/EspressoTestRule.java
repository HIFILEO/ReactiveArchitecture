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
package com.example.reactivearchitecture.util;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Throwables.throwIfUnchecked;

/**
 * Custom test runner to make sure previous activities are completed (meaning they go through
 * the entire lifecycle) before starting next test.
 *
 * There is a bug in espresso 2.2.2 where previous activity under test does NOT become destroyed
 * until the next activity test is started. This is used for for 3.0.0.
 * https://issuetracker.google.com/issues/37082857
 *
 * Inspired from:
 * https://gist.github.com/patrickhammond/19e584b90d7aae20f8f4
 *
 * @param <T>
 */
public class EspressoTestRule<T extends Activity> extends ActivityTestRule<T> {
    private static final String TAG = EspressoTestRule.class.getSimpleName();

    public EspressoTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    public EspressoTestRule(Class<T> activityClass, boolean initialTouchMode) {
        super(activityClass, initialTouchMode);
    }

    public EspressoTestRule(Class<T> activityClass, boolean initialTouchMode, boolean launchActivity) {
        super(activityClass, initialTouchMode, launchActivity);
    }

    @Override
    public Statement apply(Statement base, Description description) {

        try {
            return super.apply(base, description);
        } finally {
            closeAllActivities();
        }
    }

    // See https://code.google.com/p/android-test-kit/issues/detail?id=66
    private void closeAllActivities() {
        try {
            Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
            closeAllActivities(instrumentation);
        } catch (Exception ex) {
            Log.e(TAG, "Could not use close all activities", ex);
        }
    }

    private void closeAllActivities(Instrumentation instrumentation) throws Exception {
        final int NUMBER_OF_RETRIES = 100;
        int i = 0;
        while (closeActivity(instrumentation)) {
            if (i++ > NUMBER_OF_RETRIES) {
                throw new AssertionError("Limit of retries excesses");
            }
            Thread.sleep(200);
        }
    }

    private boolean closeActivity(Instrumentation instrumentation) throws Exception {
        final Boolean activityClosed = callOnMainSync(instrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final Set<Activity> activities = getActivitiesInStages(Stage.RESUMED,
                        Stage.STARTED, Stage.PAUSED, Stage.STOPPED, Stage.CREATED);
                activities.removeAll(getActivitiesInStages(Stage.DESTROYED));
                if (activities.size() > 0) {
                    final Activity activity = activities.iterator().next();
                    activity.finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (activityClosed) {
            instrumentation.waitForIdleSync();
        }
        return activityClosed;
    }

    private <X> X callOnMainSync(Instrumentation instrumentation, final Callable<X> callable) throws Exception {
        final AtomicReference<X> retAtomic = new AtomicReference<>();
        final AtomicReference<Throwable> exceptionAtomic = new AtomicReference<>();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                try {
                    retAtomic.set(callable.call());
                } catch (Throwable e) {
                    exceptionAtomic.set(e);
                }
            }
        });
        final Throwable exception = exceptionAtomic.get();
        if (exception != null) {
            Throwables.throwIfInstanceOf(exception, Exception.class);
            throwIfUnchecked(exception);
            throw new RuntimeException(exception);
        }
        return retAtomic.get();
    }

    public static Set<Activity> getActivitiesInStages(Stage... stages) {
        final Set<Activity> activities = Sets.newHashSet();
        final ActivityLifecycleMonitor instance = ActivityLifecycleMonitorRegistry.getInstance();
        for (Stage stage : stages) {
            final Collection<Activity> activitiesInStage = instance.getActivitiesInStage(stage);
            if (activitiesInStage != null) {
                activities.addAll(activitiesInStage);
            }
        }
        return activities;
    }
}
