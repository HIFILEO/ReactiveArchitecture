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

package com.example.reactivearchitecture.nowplaying.adapter.filter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.nowplaying.view.FilterView;

import java.util.List;

/**
 * Adapter that shows a list of {@link FilterView}.
 */
public class FilterAdapter extends ArrayAdapter<FilterView> {

    public FilterAdapter(@NonNull Context context, @NonNull List<FilterView> filterViewList) {
        super(context, 0, filterViewList);
    }

    @Override
    public @NonNull View getView(int position, View convertView, ViewGroup parent) {
        View viewToFill = convertView;
        FilterViewHolder filterViewHolder;

        if (viewToFill == null) {
            viewToFill = (LayoutInflater.from(getContext())).inflate(R.layout.filter_item, null);
            filterViewHolder = new FilterViewHolder(viewToFill);
        } else {
            filterViewHolder = (FilterViewHolder) viewToFill.getTag();
        }

        filterViewHolder.bind(getItem(position));
        viewToFill.setTag(filterViewHolder);

        return viewToFill;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View viewToFill = convertView;
        FilterViewHolder filterViewHolder;

        if (viewToFill == null) {
            viewToFill = (LayoutInflater.from(getContext())).inflate(R.layout.filter_dropdown_item, null);
            filterViewHolder = new FilterViewHolder(viewToFill);
        } else {
            filterViewHolder = (FilterViewHolder) viewToFill.getTag();
        }

        filterViewHolder.bind(getItem(position));
        viewToFill.setTag(filterViewHolder);

        return viewToFill;
    }
}
