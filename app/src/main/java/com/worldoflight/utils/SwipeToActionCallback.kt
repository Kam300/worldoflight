package com.worldoflight.ui.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.worldoflight.R

abstract class SwipeToActionCallback(private val context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // Цвета фонов
    private val deleteBackground = ColorDrawable(Color.parseColor("#FF6B6B"))
    private val quantityBackground = ColorDrawable(Color.parseColor("#4ECDC4"))

    // Иконки
    private val deleteIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_white)!!
    private val plusIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_plus_white)!!
    private val minusIcon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_minus_white)!!

    private val iconSize = 64

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

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
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(canvas, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        when {
            dX > 0 -> { // Свайп вправо - изменение количества
                drawQuantityBackground(canvas, itemView, dX)
                drawQuantityIcons(canvas, itemView, dX, itemHeight)
            }
            dX < 0 -> { // Свайп влево - удаление
                drawDeleteBackground(canvas, itemView, dX)
                drawDeleteIcon(canvas, itemView, dX, itemHeight)
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun drawQuantityBackground(canvas: Canvas, itemView: android.view.View, dX: Float) {
        quantityBackground.setBounds(
            itemView.left,
            itemView.top,
            itemView.left + dX.toInt(),
            itemView.bottom
        )
        quantityBackground.draw(canvas)
    }

    private fun drawQuantityIcons(canvas: Canvas, itemView: android.view.View, dX: Float, itemHeight: Int) {
        val iconMargin = (itemHeight - iconSize) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + iconSize

        // Иконка минус (слева)
        if (dX > iconSize * 2) {
            val minusLeft = itemView.left + iconMargin
            val minusRight = minusLeft + iconSize
            minusIcon.setBounds(minusLeft, iconTop, minusRight, iconBottom)
            minusIcon.draw(canvas)
        }

        // Иконка плюс (правее)
        if (dX > iconSize * 4) {
            val plusLeft = itemView.left + iconMargin + iconSize * 2
            val plusRight = plusLeft + iconSize
            plusIcon.setBounds(plusLeft, iconTop, plusRight, iconBottom)
            plusIcon.draw(canvas)
        }
    }

    private fun drawDeleteBackground(canvas: Canvas, itemView: android.view.View, dX: Float) {
        deleteBackground.setBounds(
            itemView.right + dX.toInt(),
            itemView.top,
            itemView.right,
            itemView.bottom
        )
        deleteBackground.draw(canvas)
    }

    private fun drawDeleteIcon(canvas: Canvas, itemView: android.view.View, dX: Float, itemHeight: Int) {
        val iconMargin = (itemHeight - iconSize) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + iconSize
        val iconLeft = itemView.right - iconMargin - iconSize
        val iconRight = itemView.right - iconMargin

        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon.draw(canvas)
    }

    private fun clearCanvas(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        canvas.drawRect(left, top, right, bottom, clearPaint)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

    // Абстрактные методы для обработки свайпов
    abstract fun onLeftSwipe(position: Int)
    abstract fun onRightSwipeAction(position: Int, action: SwipeAction)

    enum class SwipeAction {
        DECREASE, INCREASE
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition

        when (direction) {
            ItemTouchHelper.LEFT -> onLeftSwipe(position)
            ItemTouchHelper.RIGHT -> {
                // Определяем действие по расстоянию свайпа
                val swipeDistance = viewHolder.itemView.translationX
                val action = if (swipeDistance > iconSize * 3) {
                    SwipeAction.INCREASE
                } else {
                    SwipeAction.DECREASE
                }
                onRightSwipeAction(position, action)
            }
        }
    }
}
