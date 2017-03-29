package com.example.mvpexample.model;

import com.example.mvpexample.categories.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Category(UnitTest.class)
public class MovieViewInfoImplTest {
    private MovieInfoImpl movieInfo = new MovieInfoImpl(
            "www.pictureurl.com",
            "title",
            new Date(),
            8.2
    );

    @Test
    public void testMovieViewInfo() throws Exception {
        //
        //Arrange
        //
        MovieViewInfo movieViewInfo = new MovieViewInfoImpl(movieInfo);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        //
        //Act
        //

        //
        //Assert
        //
        assertThat(movieViewInfo.getTitle()).isEqualToIgnoringCase(movieInfo.getTitle());
        assertThat(movieViewInfo.getReleaseDate()).isEqualToIgnoringCase(dateFormat.format(movieInfo.getReleaseDate()));
        assertThat(movieViewInfo.getRating()).isEqualToIgnoringCase("8/10");
        assertThat(movieViewInfo.isHighRating()).isTrue();
    }

}