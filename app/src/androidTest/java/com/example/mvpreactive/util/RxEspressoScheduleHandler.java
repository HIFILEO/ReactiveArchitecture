package com.example.mvpreactive.util;

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Espresso needs an idling resource for Rx 2. THIS is that resource.
 *
 * Inspired from:
 * https://github.com/ReactiveX/RxAndroid/issues/149
 */
public class RxEspressoScheduleHandler implements Function<Runnable, Runnable> {

    private final CountingIdlingResource countingIdlingResource =
            new CountingIdlingResource("rxJava");

    @Override
    public Runnable apply(@NonNull final Runnable runnable) throws Exception {
        return new Runnable() {
            @Override
            public void run() {
                countingIdlingResource.increment();

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
