package com.nlefler.glucloser.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

public class DividerItemDecoration : RecyclerView.ItemDecoration {

    private var mDivider: Drawable? = null
    private var mShowFirstDivider = false
    private var mShowLastDivider = false

    public constructor(context: Context) {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        if (mDivider == null) {
            return
        }
        if (parent.getChildLayoutPosition(view) < 1) {
            return
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider!!.getIntrinsicHeight()
        } else {
            outRect.left = mDivider!!.getIntrinsicWidth()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (mDivider == null) {
            super.onDrawOver(c, parent, state)
            return
        }

        // Initialization needed to avoid compiler warning
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        val size: Int
        val orientation = getOrientation(parent)
        val childCount = parent.getChildCount()

        if (orientation == LinearLayoutManager.VERTICAL) {
            size = mDivider!!.getIntrinsicHeight()
            left = parent.getPaddingLeft()
            right = parent.getWidth() - parent.getPaddingRight()
        } else {
            //horizontal
            size = mDivider!!.getIntrinsicWidth()
            top = parent.getPaddingTop()
            bottom = parent.getHeight() - parent.getPaddingBottom()
        }

        for (i in (if (mShowFirstDivider) 0 else 1)..childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.getLayoutParams() as RecyclerView.LayoutParams

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getTop() - params.topMargin
                bottom = top + size
            } else {
                //horizontal
                left = child.getLeft() - params.leftMargin
                right = left + size
            }
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }

        // show last divider
        if (mShowLastDivider && childCount > 0) {
            val child = parent.getChildAt(childCount - 1)
            val params = child.getLayoutParams() as RecyclerView.LayoutParams
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getBottom() + params.bottomMargin
                bottom = top + size
            } else {
                // horizontal
                left = child.getRight() + params.rightMargin
                right = left + size
            }
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    private fun getOrientation(parent: RecyclerView): Int {
        if (parent.getLayoutManager() is LinearLayoutManager) {
            val layoutManager = parent.getLayoutManager() as LinearLayoutManager
            return layoutManager.getOrientation()
        } else {
            throw IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.")
        }
    }
}
