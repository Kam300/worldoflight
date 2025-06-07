package com.worldoflight.ui.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R

class DeleteSwipeCallback(
    private val context: Context,
    private val onDelete: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteBackground = ColorDrawable(Color.parseColor("#FF6B6B"))
    private val deleteIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_white)!!

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            onDelete(position)
        }
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val iconSize = 60

        when {
            dX > 0 -> { // Свайп вправо
                deleteBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                deleteBackground.draw(canvas)

                val iconTop = itemView.top + (itemHeight - iconSize) / 2
                val iconMargin = (itemHeight - iconSize) / 2
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + iconSize
                val iconBottom = iconTop + iconSize

                if (dX > iconSize + iconMargin) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(canvas)
                }
            }
            dX < 0 -> { // Свайп влево
                deleteBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteBackground.draw(canvas)

                val iconTop = itemView.top + (itemHeight - iconSize) / 2
                val iconMargin = (itemHeight - iconSize) / 2
                val iconLeft = itemView.right - iconMargin - iconSize
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + iconSize

                if (-dX > iconSize + iconMargin) {
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(canvas)
                }
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
}
