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

/**
 * Item to represent a filter view.
 */
public class FilterView {
    private String filterText;
    private int drawableResource;
    private boolean filterOn;

    /**
     * Constructor.
     * @param filterText - Text to show.
     * @param drawableResource - drawable resource to show
     * @param filterOn - true when filter is on, false otherwise
     */
    public FilterView(String filterText, int drawableResource, boolean filterOn) {
        this.filterText = filterText;
        this.drawableResource = drawableResource;
        this.filterOn = filterOn;
    }

    public String getFilterText() {
        return filterText;
    }

    public int getDrawableResource() {
        return drawableResource;
    }

    public boolean isFilterOn() {
        return filterOn;
    }
}
