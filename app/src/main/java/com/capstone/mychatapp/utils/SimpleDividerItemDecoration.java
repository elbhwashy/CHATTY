package com.capstone.mychatapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.capstone.mychatapp.R;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public SimpleDividerItemDecoration(Context context) {
        divider = context.getResources().getDrawable(R.drawable.recycler_horizontal_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() + 50;
        int right = parent.getWidth() - parent.getPaddingRight() - 50;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}