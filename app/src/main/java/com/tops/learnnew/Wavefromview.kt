package com.tops.learnnew

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f
        isAntiAlias = true
    }

    // thread-safe queue of amplitudes (0f..1f)
    private val amplitudes = ArrayDeque<Float>() // newest at end
    private val maxBuckets = 120 // how many bars to draw (tweak to your width)

    // call this from background or UI thread
    fun addAmplitude(a: Float) {
        val amplitude = a.coerceIn(0f, 1f)
        synchronized(amplitudes) {
            amplitudes.addLast(amplitude)
            if (amplitudes.size > maxBuckets) amplitudes.removeFirst()
        }
        // fast redraw on UI thread
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
        val barWidth = spacing * 0.6f

        for (i in list.indices) {
            val x = i * spacing + spacing / 2
            val amp = list[i]
            val barHeight = amp * h // full height scaled by amplitude
            val top = centerY - barHeight / 2
            val bottom = centerY + barHeight / 2
            canvas.drawLine(x, top, x, bottom, paint)
        }
    }
}