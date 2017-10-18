package com.example.reactivearchitecture.util;

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
    private final CountingIdlingResource countingIdlingResource;

    RxEspressoScheduleHandler(CountingIdlingResource countingIdlingResource) {
        this.countingIdlingResource = countingIdlingResource;
    }

    @Override
    public Runnable apply(@NonNull final Runnable runnable) throws Exception {
        //Note - tasks can be scheduled but not run. Ex - creating a Transformer will schedule not not execute.
        //resource to be busy from the instant the task is scheduled
        //countingIdlingResource.increment();

        return new Runnable() {
            @Override
            public void run() {
                //Note: left for understanding - resource was busy only while executing the task.
                //Note: Calling delay() would not thread sleep but schedule a wake up. So running tasks = 0; Scheduled = 1;
                countingIdlingResource.increment();

                try {
                    runnable.run();
                } finally {
                    countingIdlingResource.decrement();
                }
            }
        };
    }
}
