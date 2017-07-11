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

package com.example.mvpreactive.application;

import com.example.mvpreactive.dagger.HasInjectionProcessor;
import com.example.mvpreactive.dagger.InjectionProcessor;
import com.example.mvpreactive.dagger.DaggerTestApplicationComponent;
import com.example.mvpreactive.dagger.TestApplicationComponent;
import com.example.mvpreactive.dagger.TestApplicationModule;

/**
 * Test class for {@link MvpExampleApplication}
 */
public class TestMvpExampleApplication extends MvpExampleApplication implements HasInjectionProcessor {
    private TestApplicationComponent component;

    @Override
    void setupComponent() {
        component = DaggerTestApplicationComponent.builder()
                .testApplicationModule(new TestApplicationModule(getApplicationModule()))
                .build();
        component.inject(this);

        int a = 0;
        a++;
    }

    public TestApplicationComponent getComponent() {
        return component;
    }

    @Override
    public InjectionProcessor injectionProcessor() {
        return injectionProvider;
    }
}
