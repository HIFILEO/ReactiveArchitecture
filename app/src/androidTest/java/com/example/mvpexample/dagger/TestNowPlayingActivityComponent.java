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

package com.example.mvpexample.dagger;

import com.example.mvpexample.viewcontroller.NowPlayingActivityTest;

import dagger.Component;

/**
 * Dagger component that injects {@link com.example.mvpexample.viewcontroller.NowPlayingActivityTest} and extends
 * {@link NowPlayingActivityComponent} for testing.
 */
@ActivityScope
@Component(
        //Note - even though you are extending the other component, you still need the correct dependencies
        dependencies = TestApplicationComponent.class,
        modules = {
                TestNowPlayingActivityModule.class,
                ActivityModule.class
        })
public interface TestNowPlayingActivityComponent extends NowPlayingActivityComponent {
    //Note - the reason for extends 'NowPlayingActivityComponent', is that any automatically get the getters and
    //injectors if the 'NowPlayingActivityComponent' happened to contain any. For example, I can reuse this mock
    //component on the 'NowPlayingActivity' injection.
    void inject(NowPlayingActivityTest nowPlayingActivityTest);
}
