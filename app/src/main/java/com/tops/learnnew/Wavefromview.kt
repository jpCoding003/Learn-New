package com.tops.learnnew

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED            // ✅ Red wave
        strokeWidth = 4f             // thinner for smoother waves
        style = Paint.Style.STROKE
    }

    // Thread-safe deque of amplitudes (0f..1f)
    private val amplitudes = ArrayDeque<Float>() // newest at end
    private val maxBuckets = 200 // how many bars fit on screen

    /**
     * Add a new amplitude value (0f..1f).
     * Call this from your AudioRecord callback (scaled).
     */
    fun addAmplitude(a: Float) {
        val amplitude = a.coerceIn(0f, 1f)
        synchronized(amplitudes) {
            amplitudes.addLast(amplitude)
            if (amplitudes.size > maxBuckets) amplitudes.removeFirst() // keep left→right scroll
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

        // Draw as polyline from left → right
        var prevX = 0f
        var prevY = centerY

        for (i in list.indices) {
            val x = i * spacing
            val amp = list[i]
            val y = centerY - (amp * (h / 2f)) // amplitude to vertical offset

            if (i > 0) {
                canvas.drawLine(prevX, prevY, x, y, paint)
            }

            prevX = x
            prevY = y
        }
    }
}
