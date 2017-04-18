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

package com.example.mvpexample.viewcontroller;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.mvpexample.R;
import com.example.mvpexample.application.MvpExampleApplication;

import com.example.mvpexample.dagger.ComponentProvider;
import com.example.mvpexample.dagger.DaggerTestNowPlayingActivityComponent;
import com.example.mvpexample.dagger.NowPlayingActivityModule;
import com.example.mvpexample.dagger.TestNowPlayingActivityComponent;
import com.example.mvpexample.dagger.TestApplicationComponent;
import com.example.mvpexample.dagger.TestNowPlayingActivityModule;
import com.example.mvpexample.presenter.NowPlayingPresenter;
import com.example.mvpexample.presenter.NowPlayingPresenterImpl_IdlingResource;
import com.example.mvpexample.presenter.NowPlayingViewModel;
import com.example.mvpexample.service.ServiceApi;
import com.example.mvpexample.util.RecyclerViewItemCountAssertion;
import com.example.mvpexample.util.TestComponentProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.mockito.Matchers.any;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NowPlayingActivityTest {
    private MvpExampleApplication mvpExampleApplication;
    private ComponentProvider spyComponentProvider;

    //Note - NowPlayingActivityTest is set to scope 2.
    @Inject
    ServiceApi serviceApi;
    @Inject
    NowPlayingPresenter nowPlayingPresenter;

    @Rule
    public ActivityTestRule<NowPlayingActivity> activityTestRule = new ActivityTestRule<>(
            NowPlayingActivity.class,
            true,
            false);//do not start activity

    @Before
    public void setUp() {
        //
        //Before the activity is launched, get the componentProvider so we can provide our own module for the
        //activity under test.
        //
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mvpExampleApplication = (MvpExampleApplication)
                instrumentation.getTargetContext().getApplicationContext();

        spyComponentProvider = mvpExampleApplication.getComponentProvider();
    }

    @Test
    public void appBarShowsTitle() {
        //
        //Arrange
        //


        //
        //Act
        //
        activityTestRule.launchActivity(new Intent());

        //
        //Assert
        //
        String name = getResourceString(R.string.now_playing);
        onView(withText(name)).check(matches(isDisplayed()));
    }

    @Test
    public void progressBarShowsWhenFirstStart() {
        //
        //Arrange
        //

        //
        //Act
        //
        activityTestRule.launchActivity(new Intent());

        //
        //Assert
        //
        onView(anyOf(withId(R.id.progressBar))).check(matches(isDisplayed()));
    }

    @Test
    public void adapterHasData() {
        //
        //Arrange
        //
        //Create a component provider that can be used for testing. One part we setup the injections including
        //the activity scope on this test class. Second part where we setup our mocks before onCreate() in activity
        //under test continues.
        final ComponentProvider componentProviderForTest = new NowPlayingActivityTest_TestComponentProvider() {
            @Override
            public void setupMocks() {
                Espresso.registerIdlingResources((NowPlayingPresenterImpl_IdlingResource) nowPlayingPresenter);
            }
        };

        //When activity under test fetches the ComponentProvider, we use our test ComponentProvider instead.
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                componentProviderForTest.setupComponent((Activity) (invocation.getArguments())[0]);
                return null;
            }
        }).when(spyComponentProvider).setupComponent(any(NowPlayingActivity.class));

        //
        //Act
        //
        activityTestRule.launchActivity(new Intent());

        //
        //Assert
        //
        onView(withId(R.id.recyclerView)).check(new RecyclerViewItemCountAssertion(20));
    }

    @Test
    public void progressBarShowsWhenLoadingMoreData() {
        //
        //Arrange
        //
        //Create a component provider that can be used for testing. One part we setup the injections including
        //the activity scope on this test class. Second part where we setup our mocks before onCreate() in activity
        //under test continues.
        final ComponentProvider componentProviderForTest = new NowPlayingActivityTest_TestComponentProvider() {
            @Override
            public void setupMocks() {
                Espresso.registerIdlingResources((NowPlayingPresenterImpl_IdlingResource) nowPlayingPresenter);
            }
        };

        //When activity under test fetches the ComponentProvider, we use our test ComponentProvider instead.
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                componentProviderForTest.setupComponent((Activity) (invocation.getArguments())[0]);
                return null;
            }
        }).when(spyComponentProvider).setupComponent(any(NowPlayingActivity.class));

        //
        //Act
        //
        activityTestRule.launchActivity(new Intent());

        //
        //Assert
        //
        onView(withId(R.id.recyclerView)).check(new RecyclerViewItemCountAssertion(20));

        //unregister
        Espresso.unregisterIdlingResources((NowPlayingPresenterImpl_IdlingResource) nowPlayingPresenter);

        //Works but you need to chain two actions
        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.scrollToPosition(19),
                RecyclerViewActions.actionOnItemAtPosition(19, click())
        );

        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.scrollToPosition(20),
                RecyclerViewActions.actionOnItemAtPosition(20, click()))
                .check(matches(withId(R.id.progressBar)));

        Espresso.registerIdlingResources((NowPlayingPresenterImpl_IdlingResource) nowPlayingPresenter);
        onView(withId(R.id.recyclerView)).check(new RecyclerViewItemCountAssertion(40));
    }

    @NonNull
    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

    /**
     * {@link ComponentProvider} specific to {@link NowPlayingActivityTest} because every test will need to use the
     * same dagger injection.
     */
    public abstract class NowPlayingActivityTest_TestComponentProvider extends TestComponentProvider {
        public void inject(Activity activity) {

            NowPlayingActivityModule nowPlayingActivityModule = new NowPlayingActivityModule(
                    activity,
                    (NowPlayingViewModel) activity
            );

            TestNowPlayingActivityComponent mockNowPlayingActivityComponent = DaggerTestNowPlayingActivityComponent.builder()
                .testApplicationComponent((TestApplicationComponent) mvpExampleApplication.getComponent())
                .testNowPlayingActivityModule(new TestNowPlayingActivityModule(nowPlayingActivityModule))
                .build();

            mockNowPlayingActivityComponent.inject((NowPlayingActivity) activity);
            mockNowPlayingActivityComponent.inject(NowPlayingActivityTest.this);
        }
    }


}
