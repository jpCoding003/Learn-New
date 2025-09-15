package com.tops.learnnew

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.random.Random

class MainActivity : AppCompatActivity() {


    //            ========>   https://chatgpt.com/share/68c78086-fdd0-8010-9b4f-48c5fbc1dab7

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var waveformView: View
    private lateinit var startRecBtn: Button
    private lateinit var stopRecBtn: Button

    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        waveformView = findViewById(R.id.waveformView)
        startRecBtn = findViewById(R.id.startRecBtn)
        stopRecBtn = findViewById(R.id.stopRecBtn)

        // Default settings
        bottomSheetBehavior.peekHeight = 150
        bottomSheetBehavior.isHideable = false

        // BottomSheet state listener
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        if (isRecording) {
                            // prevent collapsing while recording
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {}
                    BottomSheetBehavior.STATE_EXPANDED -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (isRecording) {
                    // Smoothly expand waveform view with drag
                    val minHeight = 200
                    val maxHeight = 600
                    val newHeight = (minHeight + (maxHeight - minHeight) * slideOffset).toInt()
                    val params = waveformView.layoutParams
                    params.height = newHeight.coerceAtLeast(minHeight)
                    waveformView.layoutParams = params
                }
            }
        })

        // Start Recording
        startRecBtn.setOnClickListener {
            isRecording = true
            waveformView.visibility = View.VISIBLE
            startRecBtn.visibility = View.GONE
            stopRecBtn.visibility = View.VISIBLE

            // Move sheet to half-expanded
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

            // Start fake waveform animation
            startFakeWaveform()
        }

        // Stop Recording
        stopRecBtn.setOnClickListener {
            isRecording = false
            waveformView.visibility = View.GONE
            startRecBtn.visibility = View.VISIBLE
            stopRecBtn.visibility = View.GONE

            // Allow collapse again
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun startFakeWaveform() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording) {
                    // Animate waveform height randomly (simulating amplitude)
                    val params = waveformView.layoutParams
                    params.height = 200 + Random.nextInt(200)
                    waveformView.layoutParams = params
                    handler.postDelayed(this, 300)
                }
            }
        })
    }
}
