package com.worldoflight.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val horizontalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.right = horizontalSpaceHeight

        // Добавляем отступ слева только для первого элемента
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = horizontalSpaceHeight
        }
    }
}
