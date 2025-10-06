package com.tops.learnnew

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val amplitudes = ArrayDeque<Float>()
    private val maxBuckets = 120

    fun addAmplitude(a: Float) {
        val amplitude = a.coerceIn(0f, 1f)
        synchronized(amplitudes) {
            amplitudes.addLast(amplitude)
            if (amplitudes.size > maxBuckets) amplitudes.removeFirst()
        }
        postInvalidateOnAnimation()
    }

    fun clear() {
        synchronized(amplitudes) { amplitudes.clear() }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f

        val list = synchronized(amplitudes) { amplitudes.toList() }
        if (list.isEmpty()) return

        val spacing = w / list.size
        for (i in list.indices) {
            val x = i * spacing + spacing / 2f // center each bar in its slot
            val amp = list[i]
            val barHeight = amp * (h / 2f)
            canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint)
        }
    }
}
