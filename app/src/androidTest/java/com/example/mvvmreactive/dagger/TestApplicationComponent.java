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

package com.example.mvvmreactive.dagger;

import com.example.mvvmreactive.application.MvpExampleApplication;
import com.example.mvvmreactive.application.TestMvpExampleApplication;
import com.example.mvvmreactive.service.ServiceApi;
import com.example.mvvmreactive.viewcontroller.NowPlayingActivityTest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Application-level Dagger2 {@link Component} for Testing.
 */
@Singleton
@Component(
        modules = {
                TestApplicationModule.class,
                TestActivitySubComponentBuilderModule.class
        })
public interface TestApplicationComponent {
    /*
   VERY VERY IMPORTANT - You need this if you have any sub scoped components that require singleton scope access.
   In other words, the NowPlayingActivityModule required the ServiceApi object that sat in ApplicationModule.
    */
    ServiceApi getServiceApi();

    void inject(MvpExampleApplication application);

    void inject(TestMvpExampleApplication application);

    void inject(NowPlayingActivityTest nowPlayingActivityTest);
}
