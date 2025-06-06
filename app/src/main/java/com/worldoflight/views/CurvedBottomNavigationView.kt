package com.worldoflight.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.worldoflight.R

class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.background_primary)
    }

    private val path = Path()
    private val fabRadius = 120f // Радиус для выреза под FAB
    private val curveRadius = 80f // Радиус кривизны

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f

        path.reset()

        // Начинаем с левого верхнего угла с закруглением
        path.moveTo(0f, 20f)
        path.quadTo(0f, 0f, 20f, 0f)

        // Левая часть до начала выреза
        path.lineTo(centerX - fabRadius, 0f)

        // Создаем волнистый вырез
        // Первая кривая (подъем)
        path.quadTo(
            centerX - fabRadius + curveRadius, 0f,
            centerX - fabRadius + curveRadius, curveRadius / 2
        )

        // Центральная дуга (основной вырез)
        path.quadTo(
            centerX - curveRadius / 2, fabRadius / 2,
            centerX, fabRadius / 2
        )
        path.quadTo(
            centerX + curveRadius / 2, fabRadius / 2,
            centerX + fabRadius - curveRadius, curveRadius / 2
        )

        // Вторая кривая (спуск)
        path.quadTo(
            centerX + fabRadius - curveRadius, 0f,
            centerX + fabRadius, 0f
        )

        // Правая часть с закруглением
        path.lineTo(width - 20f, 0f)
        path.quadTo(width, 0f, width, 20f)

        // Правая сторона
        path.lineTo(width, height)

        // Нижняя сторона
        path.lineTo(0f, height)

        // Левая сторона
        path.lineTo(0f, 20f)

        path.close()

        canvas.drawPath(path, paint)
    }
}
