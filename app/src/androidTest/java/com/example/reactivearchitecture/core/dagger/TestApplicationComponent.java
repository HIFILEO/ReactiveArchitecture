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

package com.example.reactivearchitecture.core.dagger;

import android.app.Application;

import com.example.reactivearchitecture.core.application.TestReactiveArchitectureApplication;
import com.example.reactivearchitecture.nowplaying.viewcontroller.NowPlayingActivityTest;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Application-level Dagger2 {@link Component} for Testing.
 */
@Singleton
@Component(
        modules = {
                AndroidInjectionModule.class,
                TestApplicationModule.class,
                ActivityComponentBuilder.class
        })
public interface TestApplicationComponent extends ApplicationComponent{

    @Component.Builder
    interface Builder extends ApplicationComponent.Builder {

        //Note - If you want to pass Application to constructors of provide methods this is what you do.
        //Note - If you want to pass ReactiveArchitectureApplication to constructors of provide methods, you'll need to add it or cast.
        @BindsInstance
        TestApplicationComponent.Builder application(Application application);

        TestApplicationComponent build();
    }

    void inject(TestReactiveArchitectureApplication application);

    void inject(NowPlayingActivityTest nowPlayingActivityTest);
}
