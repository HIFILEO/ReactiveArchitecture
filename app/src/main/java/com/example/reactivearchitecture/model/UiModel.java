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

import java.util.ArrayList;
import java.util.List;

/**
 * The model the UI will bind to.
 * Note - fields in this class should be immutable for "Scan" safety.
 */
public class UiModel {
    private boolean firstTimeLoad;
    private String failureMsg;
    private int pageNumber;
    private boolean enableScrollListener;
    private List<MovieViewInfo> currentList;
    private List<MovieViewInfo> resultList;
    private @AdapterCommandType int adapterCommandType;

    /**
     * Create init state.
     * Note - this can't be a static final becuase you write espresso tests and you'll end up duplicating data.
     * @return - new UiModel in init state.
     */
    public static UiModel initState() {
        return new UiModel(true, null, 0, false, new ArrayList<MovieViewInfo>(), null, AdapterCommandType.DO_NOTHING);
    }

    /**
     * Create success state.
     * @param pageNumber - current page number.
     * @param fullList - latest full list that backs the adapter.
     * @param valuesToAdd - values to add to adapter.
     * @return new UiModel
     */
    public static UiModel successState(int pageNumber, List<MovieViewInfo> fullList, List<MovieViewInfo> valuesToAdd) {
        return new UiModel(false,
                null,
                pageNumber,
                true,
                fullList,
                valuesToAdd,
                AdapterCommandType.ADD_DATA);
    }

    /**
     * Create failure state.
     * @param pageNumber - current page number.
     * @param fullList - latest full list that backs the adapter.
     * @param failureMsg - failure message to show
     * @return new UiModel
     */
    public static UiModel failureState(int pageNumber, List<MovieViewInfo> fullList, String failureMsg) {
        return new UiModel(false,
                failureMsg,
                pageNumber,
                false,
                fullList,
                null,
                AdapterCommandType.DO_NOTHING);
    }

    /**
     * Create in progress state.
     * @param firstTimeLoad - is this first time loading in progress.
     * @param pageNumber - current page number.
     * @param fullList - latest full list that backs the adapter.
     * @return new UiModel
     */
    public static UiModel inProgressState(boolean firstTimeLoad, int pageNumber, List<MovieViewInfo> fullList) {
        return new UiModel(firstTimeLoad,
                null,
                pageNumber,
                false,
                fullList,
                null,
                firstTimeLoad ? AdapterCommandType.DO_NOTHING : AdapterCommandType.SHOW_IN_PROGRESS);
    }

    private UiModel(boolean firstTimeLoad, String failureMsg, int pageNumber, boolean enableScrollListener,
                    List<MovieViewInfo> currentList, List<MovieViewInfo> resultList, int adapterCommandType) {
        this.firstTimeLoad = firstTimeLoad;
        this.failureMsg = failureMsg;
        this.pageNumber = pageNumber;
        this.enableScrollListener = enableScrollListener;
        this.currentList = currentList;
        this.resultList = resultList;
        this.adapterCommandType = adapterCommandType;
    }

    public boolean isFirstTimeLoad() {
        return firstTimeLoad;
    }

    public String getFailureMsg() {
        return failureMsg;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public boolean isEnableScrollListener() {
        return enableScrollListener;
    }

    public List<MovieViewInfo> getCurrentList() {
        return currentList;
    }

    public List<MovieViewInfo> getResultList() {
        return resultList;
    }

    public int getAdapterCommandType() {
        return adapterCommandType;
    }
}
