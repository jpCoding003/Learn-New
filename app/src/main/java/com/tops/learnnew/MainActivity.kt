package com.tops.learnnew

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var waveformView: WaveformView
    private lateinit var btnRecord: Button

    private var audioRecord: AudioRecord? = null
    @Volatile private var isRecording = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startRecording()
            else {
                // permission denied — show message
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waveformView = findViewById(R.id.waveformView)
        btnRecord = findViewById(R.id.btnRecord)

        btnRecord.setOnClickListener {
            if (!isRecording) {
                requestPermissionAndRecord()
            } else {
                stopRecording()
            }
        }
    }

    private fun requestPermissionAndRecord() {
        when {
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startRecording() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }



        val sampleRate = 44100 // try 44100 or 48000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = minBufSize * 2
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.let { ar ->
            ar.startRecording()
            isRecording = true
            btnRecord.text = "Stop"

            lifecycleScope.launch(Dispatchers.IO) {
                val readBuffer = ShortArray(2048)
                while (isActive && isRecording) {
                    val read = ar.read(readBuffer, 0, readBuffer.size)
                    if (read > 0) {
                        // compute RMS amplitude
                        var sum = 0L
                        for (i in 0 until read) {
                            val v = readBuffer[i].toInt()
                            sum += (v * v).toLong()
                        }
                        val rms = sqrt(sum.toDouble() / read)
                        // normalize to 0..1 (since PCM16 max is 32767)
                        val amplitude = (rms / 32767.0).toFloat().coerceIn(0f, 1f)

                        // optionally smooth / low-pass filter the amplitude here

                        // push to view
                        waveformView.post { waveformView.addAmplitude(amplitude) }
                    }
                }
                // when loop ends, ensure cleanup
                ar.stop()
                ar.release()
            }
        }
    }

    private fun stopRecording() {
        isRecording = false
        audioRecord?.let {
            try {
                it.stop()
            } catch (e: Exception) { /* ignore */ }
            it.release()
            audioRecord = null
        }
        waveformView.post { waveformView.clear() }
        btnRecord.text = "Start"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}
