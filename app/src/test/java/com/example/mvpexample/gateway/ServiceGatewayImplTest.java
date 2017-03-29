package com.example.mvpexample.gateway;

import com.example.mvpexample.categories.UnitTest;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.NowPlayingInfo;
import com.example.mvpexample.service.ServiceResponse;
import com.example.mvpexample.util.TestResourceFileHelper;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.ParseException;

import static com.ibm.icu.impl.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
public class ServiceGatewayImplTest {
    private static final String IMAGE_PATH = "www.imagepath.com";

    @Test
    public void testTranslateNowPlaying() throws ParseException {
        //
        //Arrange
        //
        String json = null;
        try {
            json = TestResourceFileHelper.getFileContentAsString(this, "now_playing_page_1.json");
        } catch (Exception e) {
            fail(e);
        }
        ServiceResponse serviceResponse = new Gson().fromJson(json,  ServiceResponse.class);



        ServiceGatewayImpl.TranslateNowPlaying translateNowPlaying = new
                ServiceGatewayImpl.TranslateNowPlaying(IMAGE_PATH);

        //
        //Act
        //
        NowPlayingInfo nowPlayingInfo = translateNowPlaying.translate(serviceResponse);

        //
        //Assert
        //
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