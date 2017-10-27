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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The model the UI will bind to.
 * Note - fields in this class should be immutable for "Scan" safety.
 */
public class UiModel implements Parcelable {
    private boolean firstTimeLoad;
    private String failureMsg;
    private int pageNumber;
    private boolean enableScrollListener;
    private List<MovieViewInfo> currentList;
    private List<MovieViewInfo> resultList;
    private @AdapterCommandType int adapterCommandType;
    private boolean filterOn;

    protected UiModel(Parcel in) {
        this.firstTimeLoad = false;
        this.failureMsg = null;
        this.pageNumber = in.readInt();
        this.enableScrollListener = false;
        this.currentList = null;
        this.resultList = null;
        this.adapterCommandType = AdapterCommandType.DO_NOTHING;
        this.filterOn = in.readByte() != 0;
    }

    public static final Creator<UiModel> CREATOR = new Creator<UiModel>() {
        @Override
        public UiModel createFromParcel(Parcel in) {
            return new UiModel(in);
        }

        @Override
        public UiModel[] newArray(int size) {
            return new UiModel[size];
        }
    };

    /**
     * Create createNonInjectedData state.
     * Note - this can't be a static final because you write espresso tests and you'll end up duplicating data.
     * @return - new UiModel in createNonInjectedData state.
     */
    public static UiModel initState() {
        return new UiModel(true, null, 0, false, new ArrayList<MovieViewInfo>(), null, AdapterCommandType.DO_NOTHING, false);
    }

    private UiModel(boolean firstTimeLoad, String failureMsg, int pageNumber, boolean enableScrollListener,
                    List<MovieViewInfo> currentList, List<MovieViewInfo> resultList, int adapterCommandType, boolean filterOn) {
        this.firstTimeLoad = firstTimeLoad;
        this.failureMsg = failureMsg;
        this.pageNumber = pageNumber;
        this.enableScrollListener = enableScrollListener;
        this.currentList = currentList;
        this.resultList = resultList;
        this.adapterCommandType = adapterCommandType;
        this.filterOn = filterOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        //only care about pageNumber when saving state
        parcel.writeInt(pageNumber);
        parcel.writeByte((byte) (filterOn ? 1 : 0));
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

    /**
     * Return a shallow copy of the current list.
     * @return Shallow copy of list.
     */
    public List<MovieViewInfo> getCurrentList() {
        return new ArrayList<>(currentList);
    }

    public List<MovieViewInfo> getResultList() {
        return resultList;
    }

    public int getAdapterCommandType() {
        return adapterCommandType;
    }

    public boolean isFilterOn() {
        return filterOn;
    }

    /**
     * Too many state? Too many params in constructors? Call on the builder pattern to Save The Day!.
     */
    public static class UiModelBuilder {
        private final UiModel uiModel;

        private boolean firstTimeLoad;
        private String failureMsg;
        private int pageNumber;
        private boolean enableScrollListener;
        private List<MovieViewInfo> currentList;
        private List<MovieViewInfo> resultList;
        private @AdapterCommandType int adapterCommandType;
        private boolean filterOn;

        /**
         * Construct Builder using defaults from previous {@link UiModel}.
         * @param uiModel - model for builder to use.
         */
        public UiModelBuilder(@NonNull UiModel uiModel) {
            this.uiModel = uiModel;

            this.firstTimeLoad = uiModel.firstTimeLoad;
            this.failureMsg = uiModel.failureMsg;
            this.pageNumber = uiModel.pageNumber;
            this.enableScrollListener = uiModel.enableScrollListener;
            this.currentList = uiModel.currentList;
            this.resultList = uiModel.resultList;
            this.adapterCommandType = uiModel.adapterCommandType;
            this.filterOn = uiModel.filterOn;
        }

        public UiModelBuilder() {
            this.uiModel = null;
        }

        /**
         * Create the {@link UiModel} using the types in {@link UiModelBuilder}.
         * @return new {@link UiModel}.
         */
        public UiModel createUiModel() {
            if (currentList == null) {
                if (uiModel == null) {
                    currentList = new ArrayList<>();
                } else {
                    //shallow copy
                    currentList = uiModel.getCurrentList();
                }
            }

            return new UiModel(
                    firstTimeLoad,
                    failureMsg,
                    pageNumber,
                    enableScrollListener,
                    currentList,
                    resultList,
                    adapterCommandType,
                    filterOn);
        }

        public UiModelBuilder setFirstTimeLoad(boolean firstTimeLoad) {
            this.firstTimeLoad = firstTimeLoad;
            return this;
        }

        public UiModelBuilder setFailureMsg(String failureMsg) {
            this.failureMsg = failureMsg;
            return this;
        }

        public UiModelBuilder setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public UiModelBuilder setEnableScrollListener(boolean enableScrollListener) {
            this.enableScrollListener = enableScrollListener;
            return this;
        }

        public UiModelBuilder setCurrentList(List<MovieViewInfo> currentList) {
            this.currentList = currentList;
            return this;
        }

        public UiModelBuilder setResultList(List<MovieViewInfo> resultList) {
            this.resultList = resultList;
            return this;
        }

        public UiModelBuilder setAdapterCommandType(int adapterCommandType) {
            this.adapterCommandType = adapterCommandType;
            return this;
        }

        public UiModelBuilder setFilterOn(boolean filterOn) {
            this.filterOn = filterOn;
            return this;
        }
    }
}
