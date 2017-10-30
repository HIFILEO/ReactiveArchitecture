package com.example.reactivearchitecture.nowplaying.controller;

import com.example.reactivearchitecture.categories.UnitTest;
import com.example.reactivearchitecture.nowplaying.model.MovieInfo;
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo;
import com.example.reactivearchitecture.rx.RxJavaTest;
import com.example.reactivearchitecture.nowplaying.service.ServiceResponse;
import com.example.reactivearchitecture.util.TestResourceFileHelper;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import io.reactivex.observers.TestObserver;

import static com.ibm.icu.impl.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
public class ServiceControllerImplTest extends RxJavaTest {
    private static final String IMAGE_PATH = "www.imagepath.com";

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testTranslateNowPlaying() throws Exception {
        //
        //Arrange
        //
        TestObserver<NowPlayingInfo> testObserver;
        String json = null;
        try {
            json = TestResourceFileHelper.getFileContentAsString(this, "now_playing_page_1.json");
        } catch (Exception e) {
            fail(e);
        }
        ServiceResponse serviceResponse = new Gson().fromJson(json,  ServiceResponse.class);

        ServiceControllerImpl.TranslateNowPlayingSubscriptionFunc translateNowPlayingSubscriptionFunc
                = new ServiceControllerImpl.TranslateNowPlayingSubscriptionFunc(IMAGE_PATH);

        //
        //Act
        //
        testObserver = translateNowPlayingSubscriptionFunc.apply(serviceResponse).test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        NowPlayingInfo nowPlayingInfo = (NowPlayingInfo) testObserver.getEvents().get(0).get(0);
        assertThat(nowPlayingInfo).isNotNull();
        assertThat(nowPlayingInfo.getPageNumber()).isEqualTo(1);
        assertThat(nowPlayingInfo.getTotalPageNumber()).isEqualTo(35);
        assertThat(nowPlayingInfo.getMovies().size()).isEqualTo(20);
        assertThat(nowPlayingInfo.getMovies()).isNotNull();

        MovieInfo movieInfo = nowPlayingInfo.getMovies().get(0);
        assertThat(movieInfo.getPictureUrl()).isEqualToIgnoringCase(IMAGE_PATH + "/tnmL0g604PDRJwGJ5fsUSYKFo9.jpg");
        assertThat(movieInfo.getRating()).isEqualTo(7.2);
        assertThat(movieInfo.getReleaseDate().toString()).isEqualToIgnoringCase("Wed Mar 15 00:00:00 EDT 2017");
        assertThat(movieInfo.getTitle()).isEqualToIgnoringCase("Beauty and the Beast");
    }

}