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

package com.example.reactivearchitecture.model;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * View representation of movie information. The business logic for data manipulation that is
 * handled by the view data.
 */
public class MovieViewInfoImpl implements MovieViewInfo {
    @SuppressLint("SimpleDateFormat")
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private MovieInfo movieInfo;

    public MovieViewInfoImpl(MovieInfo movieInfo) {
        this.movieInfo = movieInfo;
    }

    @Override
    public String getPictureUrl() {
        return movieInfo.getPictureUrl();
    }

    @Override
    public String getTitle() {
        return movieInfo.getTitle();
    }

    @Override
    public String getReleaseDate() {
        return dateFormat.format(movieInfo.getReleaseDate());
    }

    @Override
    public String getRating() {
        return String.valueOf(Math.round(movieInfo.getRating())) + "/10";
    }

    @Override
    public boolean isHighRating() {
        return Math.round(movieInfo.getRating()) >= FilterManager.RATE_NUMBER_TO_STAR;
    }
}
