package com.tops.learnnew

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tops.learnnew.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
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
                    val params = binding.waveformView.layoutParams
                    params.height = newHeight.coerceAtLeast(minHeight)
                    binding.waveformView.layoutParams = params
                }
            }
        })

        binding.myswitch.setOnClickListener {
           val intent = Intent(this, RecordingActivity::class.java)
            startActivity(intent)
        }

        // Start Recording
        binding.startRecBtn.setOnClickListener {
            isRecording = true
            binding.waveformView.visibility = View.VISIBLE
            binding.startRecBtn.visibility = View.GONE
            binding.stopRecBtn.visibility = View.VISIBLE

            // Move sheet to half-expanded
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

            // Start fake waveform animation
            startFakeWaveform()
        }

        // Stop Recording
        binding.stopRecBtn.setOnClickListener {
            isRecording = false
            binding.waveformView.visibility = View.GONE
            binding.startRecBtn.visibility = View.VISIBLE
            binding.stopRecBtn.visibility = View.GONE

            // Allow collapse again
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun startFakeWaveform() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording) {
                    // Animate waveform height randomly (simulating amplitude)
                    val params = binding.waveformView.layoutParams
                    params.height = 200 + Random.nextInt(200)
                    binding.waveformView.layoutParams = params
                    handler.postDelayed(this, 300)
                }
            }
        })
    }
}
