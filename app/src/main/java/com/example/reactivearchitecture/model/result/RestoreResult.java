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

package com.example.reactivearchitecture.model.result;

import com.example.reactivearchitecture.model.MovieInfo;

import java.util.List;

/**
 * Results from {@link com.example.reactivearchitecture.model.action.RestoreAction} requests.
 */
public class RestoreResult extends Result {
    private @ResultType int resultType;
    private boolean isSuccessful;
    private boolean isLoading;
    private int pageNumber;
    private List<MovieInfo> result;
    private Throwable error;

    public static RestoreResult inFlight(int pageNumber, List<MovieInfo> result) {
        return new RestoreResult(ResultType.IN_FLIGHT, true, true, pageNumber, result, null);
    }

    public static RestoreResult sucess(int pageNumber, List<MovieInfo> result) {
        return new RestoreResult(ResultType.SUCCESS, true, false, pageNumber, result, null);
    }

    public static RestoreResult failure(int pageNumber, boolean inProgress, Throwable error) {
        return new RestoreResult(ResultType.FAILURE, false, inProgress, pageNumber, null, error);
    }

    /**
     * Constructor.
     * @param resultType -
     * @param isSuccessful -
     * @param isLoading -
     * @param pageNumber -
     * @param result -
     * @param error -
     */
    public RestoreResult(int resultType, boolean isSuccessful, boolean isLoading, int pageNumber,
                          List<MovieInfo> result, Throwable error) {
        this.resultType = resultType;
        this.isSuccessful = isSuccessful;
        this.isLoading = isLoading;
        this.pageNumber = pageNumber;
        this.result = result;
        this.error = error;
    }

    @Override
    public int getType() {
        return resultType;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public List<MovieInfo> getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }
}
