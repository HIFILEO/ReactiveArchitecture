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
 * Manages all data. Filters data based on those that have an eight or above rating.
 */
public class FilterManager {
    public static final int RATE_NUMBER_TO_STAR = 8;
    private final List<MovieInfo> fullList = new ArrayList<>();
    private final List<MovieInfo> filteredList = new ArrayList<>();
    private boolean filterOn;

    /**
     * Constructor.
     * @param filterOn - true to turn filter on, false otherwise.
     */
    public FilterManager(boolean filterOn) {
        this.filterOn = filterOn;
    }

    /**
     * Filter a list of data. Retrains all entries passed in.
     * @param listToFilter -  list to retain as well as filter.
     * @return a new filtered list or original un-filtered list that was  passed in.
     */
    public synchronized List<MovieInfo> filterList(List<MovieInfo> listToFilter) {
        fullList.addAll(listToFilter);

        List<MovieInfo> filteredData = filterData(listToFilter);
        if (filterOn) {
            return filteredData;
        } else {
            return listToFilter;
        }
    }

    public synchronized void setFilterOn(boolean filterOn) {
        this.filterOn = filterOn;
    }

    /**
     * Get full list based on {@link FilterManager#filterOn}.
     * @return - full list of filtered or unfiltered data.
     */
    public synchronized List<MovieInfo> getFullList() {
        if (filterOn) {
            return new ArrayList<>(filteredList);
        } else {
            return new ArrayList<>(fullList);
        }
    }

    /**
     * Is filter on?
     * @return true is yes, false otherwise.
     */
    public boolean isFilterOn() {
        return filterOn;
    }

    /**
     * Filter data, and store for later use. Return filtered list.
     * @param listToFilter - list to filter.
     * @return filtered list.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private List<MovieInfo> filterData(List<MovieInfo> listToFilter) {
        List<MovieInfo> filteredList = new ArrayList<>();

        for (MovieInfo movieInfo : listToFilter) {
            if (Math.round(movieInfo.getRating()) >= RATE_NUMBER_TO_STAR) {
                filteredList.add(movieInfo);
            }
        }

        this.filteredList.addAll(filteredList);

        return filteredList;
    }
}
