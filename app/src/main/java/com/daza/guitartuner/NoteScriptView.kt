package com.daza.guitartuner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class NoteStripView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    private var position = 0f  // posición en semitonos, de 0 a totalSteps

    private val totalSteps = noteNames.size  // 12

    fun updatePosition(pos: Float) {
        position = pos.coerceIn(0f, totalSteps.toFloat())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#1E1E1E"))

        val widthPerStep = width / totalSteps.toFloat()

        // Líneas divisorias
        paint.color = Color.DKGRAY
        paint.strokeWidth = 2f
        for (i in 0..totalSteps) {
            val x = i * widthPerStep
            canvas.drawLine(x, 0f, x, height.toFloat(), paint)
        }

        // Nombres de las notas centrados en cada paso
        paint.color = Color.LTGRAY
        paint.textSize = 28f
        paint.textAlign = Paint.Align.CENTER
        for (i in noteNames.indices) {
            val x = (i + 0.5f) * widthPerStep
            canvas.drawText(noteNames[i], x, height - 12f, paint)
        }

        // Indicador rojo centrado sobre el paso
        val indicatorX = (position + 0.5f) * widthPerStep
        paint.color = Color.RED
        paint.strokeWidth = 6f
        canvas.drawLine(indicatorX, 0f, indicatorX, height.toFloat(), paint)
    }

}
