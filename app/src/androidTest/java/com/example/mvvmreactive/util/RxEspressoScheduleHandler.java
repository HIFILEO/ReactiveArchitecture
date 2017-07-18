package com.example.mvvmreactive.util;

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Espresso needs an idling resource for Rx 2. THIS is that resource.
 *
 * Inspired from:
 * https://github.com/ReactiveX/RxAndroid/issues/149
 *
 * Modified to tell espresso we are busy the instant any task is scheduled and only idle once
 * all scheduled tasks are completed.
 */
public class RxEspressoScheduleHandler implements Function<Runnable, Runnable> {

    private final CountingIdlingResource countingIdlingResource =
            new CountingIdlingResource("rxJava");

    @Override
    public Runnable apply(@NonNull final Runnable runnable) throws Exception {
        //resource to be busy from the instant the task is scheduled
        countingIdlingResource.increment();

        return new Runnable() {
            @Override
            public void run() {
                //Note: left for understanding - resource was busy only while executing the task
                //countingIdlingResource.increment();

                try {
                    runnable.run();
                } finally {
                    countingIdlingResource.decrement();
                }
            }
        };
    }

    public IdlingResource getIdlingResource() {
        return countingIdlingResource;
    }

}
