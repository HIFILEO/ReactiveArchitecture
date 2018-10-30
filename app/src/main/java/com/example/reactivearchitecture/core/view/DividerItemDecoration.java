// CHECKSTYLE:OFF
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reactivearchitecture.core.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * I don't know why Google does this sometimes,
 * but here's a class to put dividers in recycler view.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
        android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    private Drawable dividerDrawable;
    private int orientation;
    private int color;

    private int additionalLeftPadding;
    private int additionalTopPadding;
    private int additionalRightPadding;
    private int additionalBottomPadding;

    /**
     * constructor needs a context and an orientation.
     * Looks like it defines the values you can use
     * @param context a context
     * @param orientation orientation as defined in the contants
     * @param color the color of the divider
     */
    public DividerItemDecoration(Context context, int orientation, int color) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        dividerDrawable = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
        this.color = color;
    }

    /**
     * Constructor for an RecyclerView.ItemDecoration that can be used for item spacing.
     * @param orientation VERTICAL_LIST or HORIZONTAL_LIST
     * @param color color to set the dividerDrawable
     * @param dividerDrawable the Drawable to use as the divider.
     */
    public DividerItemDecoration(int orientation, int color, Drawable dividerDrawable) {
        this.dividerDrawable = dividerDrawable;
        this.color = color;
        setOrientation(orientation);
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, dividerDrawable.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, dividerDrawable.getIntrinsicWidth(), 0);
        }
    }

    /**
     * Set orientation on this view.
     * @param orientation  HORZ or VERT as defined above
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        this.orientation = orientation;
    }

    /**
     * Draws a vertical divider.
     * @param c a canvas
     * @param parent the recycler view
     */
    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + additionalLeftPadding;
        final int right = parent.getWidth() - parent.getPaddingRight() - additionalRightPadding;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin + additionalTopPadding;
            final int bottom = top + dividerDrawable.getIntrinsicHeight() - additionalBottomPadding;
            dividerDrawable.setBounds(left, top, right, bottom);
            dividerDrawable.setColorFilter(color, PorterDuff.Mode.SRC);
            dividerDrawable.draw(c);
        }
    }

    /**
     * Draw a horizontal divider.
     * @param c a canvas
     * @param parent the recycler view
     */
    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop() + additionalTopPadding;
        final int bottom = parent.getHeight() - parent.getPaddingBottom() - additionalBottomPadding;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin + additionalLeftPadding;
            final int right = left + dividerDrawable.getIntrinsicWidth() - additionalRightPadding;
            dividerDrawable.setBounds(left, top, right, bottom);
            dividerDrawable.setColorFilter(color, PorterDuff.Mode.SRC);
            dividerDrawable.draw(c);
        }
    }

    /**
     * Add optional, additional padding to the decorator if you want it to inset from the Recycle View itself.
     * @param left -
     * @param top -
     * @param right -
     * @param bottom -
     */
    public void setAdditionalPadding(int left, int top, int right, int bottom) {
        additionalLeftPadding = left;
        additionalTopPadding = top;
        additionalRightPadding = right;
        additionalBottomPadding = bottom;
    }
}
