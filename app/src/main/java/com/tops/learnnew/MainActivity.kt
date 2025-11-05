package com.tops.learnnew

import Recording
import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tops.learnnew.databinding.ActivityCurrencyConverterBinding
import com.tops.learnnew.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    // API :   "https://api.frankfurter.app/"
//      https://youtu.be/uY9iZiamyZs?si=hqf0_p7pwhbCG_Uq

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            // handle result — simple approach: do nothing, user must grant to record
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // request runtime permissions
        requestPermissions.launch(arrayOf(Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight = 150
        bottomSheetBehavior.isHideable = false


        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        if (isRecording) {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                    else -> { /* no-op */ }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (isRecording) {
                    val minHeight = 200
                    val maxHeight = 600
                    val newHeight = (minHeight + (maxHeight - minHeight) * (slideOffset.coerceIn(0f,1f))).toInt()
                    val params = binding.waveformView.layoutParams
                    params.height = newHeight.coerceAtLeast(minHeight)
                    binding.waveformView.layoutParams = params
                }
            }
        })

        binding.myswitch.setOnClickListener {
            val intent = Intent(this, RecordingListActivity::class.java)
            startActivity(intent)
        }

        binding.Drawer.setOnClickListener { startActivity(Intent(this,DrawerActivity::class.java)) }
        // Start Recording
        binding.startRecBtn.setOnClickListener {
            startRecording()
            binding.waveformView.visibility = View.VISIBLE
            binding.startRecBtn.visibility = View.GONE
            binding.stopRecBtn.visibility = View.VISIBLE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        binding.btnconvert.setOnClickListener {  val intent = Intent(this,
            CurrencyConverterActivity::class.java)
            startActivity(intent) }

        // Stop Recording
        binding.stopRecBtn.setOnClickListener {
            val path = stopRecording()
            binding.waveformView.visibility = View.GONE
            binding.startRecBtn.visibility = View.VISIBLE
            binding.stopRecBtn.visibility = View.GONE
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            if (path != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance(this@MainActivity)
                        .recordingDao()
                        .insert(Recording(filePath = path, timestamp = System.currentTimeMillis()))
                }
            }
        }
    }

    private fun startRecording() {
        val dir = File(getExternalFilesDir(null), "recordings")
        if (!dir.exists()) dir.mkdirs()

        audioFile = File(dir, "rec_${System.currentTimeMillis()}.mp4")

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        isRecording = true
        binding.waveformView.clear()
        startAmplitudeUpdates()
    }

    private fun startAmplitudeUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val amp = mediaRecorder?.maxAmplitude ?: 0
                    // Normalize maxAmplitude (0..32767) - safe divide avoid 0
                    val norm = (amp / 32767f).coerceIn(0f, 1f)
                    binding.waveformView.addAmplitude(norm)
                    handler.postDelayed(this, 100)
                }
            }
        })
    }

    private fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            audioFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaRecorder?.release()
    }
}
