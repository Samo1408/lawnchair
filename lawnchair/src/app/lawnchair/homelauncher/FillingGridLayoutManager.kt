/*
 * FillingGridLayoutManager for Lawnchair Workspace
 * 
 * A RecyclerView GridLayoutManager that forces every child cell to fill
 * an exact slice of the available area so that `cols × rows` cells
 * perfectly cover the page regardless of icon sizes.
 * 
 * Usage: Set as the layout manager for any RecyclerView grid.
 *   rv.layoutManager = FillingGridLayoutManager(context, cols, rows)
 */

package app.lawnchair.homelauncher

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FillingGridLayoutManager(
    context: Context,
    columns: Int,
    private val rows: Int = 5,
) : GridLayoutManager(context, columns) {

    init {
        isItemPrefetchEnabled = false
    }

    override fun canScrollVertically(): Boolean = false
    override fun canScrollHorizontally(): Boolean = false

    private fun fillLayoutParams(lp: RecyclerView.LayoutParams): RecyclerView.LayoutParams {
        val usableWidth = width - paddingLeft - paddingRight
        val usableHeight = height - paddingTop - paddingBottom

        lp.width = if (usableWidth > 0) usableWidth / spanCount
                   else ViewGroup.LayoutParams.MATCH_PARENT

        lp.height = if (usableHeight > 0 && rows > 0) usableHeight / rows
                    else ViewGroup.LayoutParams.WRAP_CONTENT

        return lp
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        fillLayoutParams(super.generateDefaultLayoutParams() as RecyclerView.LayoutParams)

    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams =
        fillLayoutParams(super.generateLayoutParams(c, attrs) as RecyclerView.LayoutParams)

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams =
        fillLayoutParams(super.generateLayoutParams(lp) as RecyclerView.LayoutParams)
}
