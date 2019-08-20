package com.codegames.farsroid.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.round

@Suppress("MemberVisibilityCanBePrivate", "CAST_NEVER_SUCCEEDS")
class SpacesItemDecoration(val space: Int, val orientation: Int = -1) : RecyclerView.ItemDecoration() {

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        const val GRID = 2
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {


        val position = parent.getChildAdapterPosition(view)

        when (orientation) {
            HORIZONTAL -> outRect.set(calculateHorizontalSpace(position))
            VERTICAL -> outRect.set(calculateVerticalSpace(position))
            GRID -> {
                val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
                outRect.set(calculateGridSpace(spanCount, position))
            }
            else -> outRect.set(space, space, space, space)
        }

    }

    private fun calculateGridSpace(spanCount: Int, position: Int): Rect {

        val rowPos = position % spanCount

        val all = (spanCount.toFloat() + 1) / spanCount * space

        val left = (rowPos + 1) * space - rowPos * all
        val right = all - left

        val top = if (position in 0 until spanCount) space else 0

        return Rect(round(left).toInt(), top, round(right).toInt(), space)
    }

    private fun calculateVerticalSpace(position: Int): Rect {
        val top = if (position == 0) space else 0
        return Rect(space, top, space, space)
    }

    private fun calculateHorizontalSpace(position: Int): Rect {
        val left = if (position == 0) space else 0
        return Rect(left, space, space, space)
    }

}
