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

import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * Rx Handler for idling. Read the following to understand how idling is calculated:
 * {@link RxEspressoObserverHandler}
 * {@link RxEspressoScheduleHandler}
 */
public class RxEspressoHandler {
    private final CountingIdlingResource countingIdlingResource =
            new CountingIdlingResource(RxEspressoHandler.class.getSimpleName(), true);

    private RxEspressoScheduleHandler rxEspressoScheduleHandler;
    private RxEspressoObserverHandler rxEspressoObserverHandler;

    public RxEspressoHandler() {
        rxEspressoScheduleHandler = new RxEspressoScheduleHandler(countingIdlingResource);
        rxEspressoObserverHandler = new RxEspressoObserverHandler(countingIdlingResource);
    }

    public IdlingResource getIdlingResource() {
        return countingIdlingResource;
    }

    public RxEspressoScheduleHandler getRxEspressoScheduleHandler() {
        return rxEspressoScheduleHandler;
    }

    public RxEspressoObserverHandler getRxEspressoObserverHandler() {
        return rxEspressoObserverHandler;
    }
}
