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
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    private val deleteBackground = ColorDrawable(Color.parseColor("#FF6B6B"))
    private val deleteIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_white)!!

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onDelete(position)
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

        if (dX > 0) { // Свайп вправо - удаление
            deleteBackground.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            deleteBackground.draw(canvas)

            // Рисуем иконку удаления
            val iconTop = itemView.top + (itemHeight - 60) / 2
            val iconBottom = iconTop + 60
            val iconLeft = itemView.left + 40
            val iconRight = iconLeft + 60

            if (dX > 100) { // Показываем иконку только при достаточном свайпе
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(canvas)
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
}
