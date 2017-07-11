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

package com.example.mvpreactive.viewcontroller;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.mvpreactive.R;

import com.example.mvpreactive.application.TestMvpExampleApplication;
import com.example.mvpreactive.dagger.InjectionProcessor;
import com.example.mvpreactive.service.ServiceApi;
import com.example.mvpreactive.service.ServiceResponse;
import com.example.mvpreactive.util.EspressoTestRule;
import com.example.mvpreactive.util.RecyclerViewItemCountAssertion;
import com.example.mvpreactive.util.RecyclerViewMatcher;
import com.example.mvpreactive.util.RxEspressoScheduleHandler;
import com.example.mvpreactive.util.TestEspressoAssetFileHelper;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.plugins.RxJavaPlugins;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NowPlayingActivityTest {
    private static TestMvpExampleApplication testMvpExampleApplication;
    private static ServiceResponse serviceResponse1;
    private static ServiceResponse serviceResponse2;
    private static Map<String, Integer> mapToSend1 = new HashMap<>();
    private static Map<String, Integer> mapToSend2 = new HashMap<>();

    @Inject
    ServiceApi serviceApi;

    @Rule
    public EspressoTestRule<NowPlayingActivity> activityTestRule = new EspressoTestRule<>(
            NowPlayingActivity.class,
            true,
            false);//do not start activity

    /**
     * Convenience helper to create matcher for recycler view.
     * @param recyclerViewId - ID of {@link android.support.v7.widget.RecyclerView}
     * @return {@link RecyclerViewMatcher}
     */
    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    @BeforeClass
    public static void setUpClass() {
        //
        //Application Mocking setup, once for all tests in this example
        //

        //Before the activity is launched, get the componentProvider so we can provide our own
        //module for the activity under test.
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        testMvpExampleApplication = (TestMvpExampleApplication)
                instrumentation.getTargetContext().getApplicationContext();

        //Load JSON data you plan to test with
        //Note - if you wanted to just load it once per test you move this logic.
        String json = null;
        String json2 = null;
        try {
            json = TestEspressoAssetFileHelper.getFileContentAsString(
                    InstrumentationRegistry.getContext(),
                    "now_playing_page_1.json");
            json2 = TestEspressoAssetFileHelper.getFileContentAsString(
                    InstrumentationRegistry.getContext(),
                    "now_playing_page_2.json");
        } catch (Exception e) {
            fail(e.toString());
        }

        serviceResponse1 = new Gson().fromJson(json,  ServiceResponse.class);
        serviceResponse2 = new Gson().fromJson(json2,  ServiceResponse.class);

        mapToSend1.put("page", 1);
        mapToSend2.put("page", 2);
    }

    @Before
    public void setup() {
        //Inject all the application level objects into this test class
        testMvpExampleApplication.getComponent().inject(this);

        //Every test uses the same JSON response so set it up here once.
        try {
            setupServiceApiMockForData();
        } catch (IOException e) {
            fail(e.toString());
        }
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
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void adapterHasData() {
        //
        //Arrange
        //

        //Register Rx Idling (Only needed for specific tests that need to wait for data)
        RxEspressoScheduleHandler rxEspressoScheduleHandler = new RxEspressoScheduleHandler();
        RxJavaPlugins.setScheduleHandler(rxEspressoScheduleHandler);
        Espresso.registerIdlingResources(rxEspressoScheduleHandler.getIdlingResource());

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

        //Register Rx Idling (Only needed for specific tests that need to wait for data)
        RxEspressoScheduleHandler rxEspressoScheduleHandler = new RxEspressoScheduleHandler();
        RxJavaPlugins.setScheduleHandler(rxEspressoScheduleHandler);
        Espresso.registerIdlingResources(rxEspressoScheduleHandler.getIdlingResource());

        //
        //Act
        //
        activityTestRule.launchActivity(new Intent());

        //
        //Assert
        //
        //Note - because of the idling resource, this check will wait until data is loaded.
        onView(withId(R.id.recyclerView)).check(new RecyclerViewItemCountAssertion(20));

        //unregister so we can do checks without waiting for data
        Espresso.unregisterIdlingResources(rxEspressoScheduleHandler.getIdlingResource());

        //Scroll to the bottom to trigger the progress par.
        onView(withId(R.id.recyclerView)).perform(
                RecyclerViewActions.scrollToPosition(19),//scroll to bottom so progress spinner gets added
                RecyclerViewActions.scrollToPosition(20) //scroll to show progress spinner
        );

        //Note - without idling resource, we can check for progress item while data loads.
        onView(withId(R.id.recyclerView)).check(new RecyclerViewItemCountAssertion(21));

        onView(withRecyclerView(R.id.recyclerView).atPosition(20))
                .check(matches(hasDescendant(withId(R.id.progressBar))));
    }

    @After
    public void tearDown() {
        //Note - you must remove the idling resources after each Test
        List<IdlingResource> idlingResourceList = Espresso.getIdlingResources();
        if (idlingResourceList != null) {
            for (int i = 0; i < idlingResourceList.size(); i++) {
                Espresso.unregisterIdlingResources(idlingResourceList.get(i));
            }
        }
    }

    @NonNull
    private String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

    /**
     * Setup the serviceApi mock to mock backend calls.
     * @throws IOException -
     */
    private void setupServiceApiMockForData() throws IOException {
        //Since the 'serviceApi' is an application singleton, we must reset it for every test.
        Mockito.reset(serviceApi);

        String apiKey = getResourceString(R.string.api_key);
        when(serviceApi.nowPlaying(apiKey, mapToSend1)).thenReturn(Observable.just(serviceResponse1));
        when(serviceApi.nowPlaying(apiKey, mapToSend2)).thenReturn(Observable.just(serviceResponse2));
    }
}
