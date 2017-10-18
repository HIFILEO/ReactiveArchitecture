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

import android.support.test.espresso.idling.CountingIdlingResource;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.observable.ObservableDelay;

/**
 * Check observables that are triggered in Rx. For those that are Delays, you need to increment {@link CountingIdlingResource}
 * because {@link RxEspressoScheduleHandler} only tracks "running" resources.
 */
public class RxEspressoObserverHandler implements Function<Observable, Observable> {
    private final CountingIdlingResource countingIdlingResource;

    RxEspressoObserverHandler(CountingIdlingResource countingIdlingResource) {
        this.countingIdlingResource = countingIdlingResource;
    }

    @Override
    public Observable apply(@NonNull Observable observable) throws Exception {
        /**
         * ObservableDelay<?> need to count for idling.
         */
        if (observable instanceof ObservableDelay) {
            countingIdlingResource.increment();

            //Use reflection since ObservableDelay uses source to trigger actual scheduled wait.
            ObservableDelay<?> observableDelay = (ObservableDelay<?>) observable;
            Field delayPrivateField = ObservableDelay.class.getDeclaredField("delay");
            Field timeUnitsPrivateField = ObservableDelay.class.getDeclaredField("unit");

            delayPrivateField.setAccessible(true);
            timeUnitsPrivateField.setAccessible(true);

            long delay = delayPrivateField.getLong(observableDelay);
            TimeUnit unit = (TimeUnit) timeUnitsPrivateField.get(observableDelay);

            //Create timer task to decrement delay.
            //Note - tack on 100ms for thread switching since this is started before subscription actually triggers.
            TimerTask timeTask = new TimerTask() {
                @Override
                public void run() {
                    countingIdlingResource.decrement();
                }
            };
            Timer timer = new Timer();
            long milliseconds = TimeUnit.MILLISECONDS.convert(delay, unit) + 100;
            timer.schedule(timeTask, milliseconds);
        }

        return observable;
    }
}
