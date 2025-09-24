package com.tops.learnnew

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tops.learnnew.databinding.ActivityMainBinding
import com.tops.learnnew.databinding.ActivityRecordingBinding
import kotlin.random.Random

class RecordingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val handler = Handler(Looper.getMainLooper())

    private var isRecording = false
    private var isPaused = false
    private var seconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = 150
        bottomSheetBehavior.isHideable = false

        // Start recording
        binding.startRecBtn.setOnClickListener {
            isRecording = true
            seconds = 0

            binding.recordingLabel.visibility = View.VISIBLE
            binding.recordingTimer.visibility = View.VISIBLE
            binding.waveformView.visibility = View.VISIBLE
            binding.pauseResumeBtn.visibility = View.VISIBLE

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

            startTimer()
            startFakeWaveform()
        }

        // Pause / Resume
        binding.pauseResumeBtn.setOnClickListener {
            if (isPaused) {
                isPaused = false
                binding.pauseResumeBtn.text = "Pause"
                startTimer()
                startFakeWaveform()
            } else {
                isPaused = true
                binding.pauseResumeBtn.text = "Resume"
            }
        }

        // BottomSheet expand listener
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Show full UI
                        binding.halfContent.visibility = View.GONE
                        binding.expandedContent.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // Show half UI
                        binding.halfContent.visibility = View.VISIBLE
                        binding.expandedContent.visibility = View.GONE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording && !isPaused) {
                    seconds++
                    val mins = seconds / 60
                    val secs = seconds % 60
                    binding.recordingTimer.text = String.format("%02d:%02d", mins, secs)
                    binding.fullTimer.text = String.format("%02d:%02d", mins, secs)
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun startFakeWaveform() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording && !isPaused) {
                    val randomAmp = Random.nextFloat()
                    binding.waveformView.addAmplitude(randomAmp)
                    binding.fullWaveformView.addAmplitude(randomAmp)
                    handler.postDelayed(this, 200)
                }
            }
        })
    }
}
