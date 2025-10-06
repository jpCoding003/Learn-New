package com.tops.learnnew

import Recording
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tops.learnnew.databinding.ActivityRecordingListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RecordingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordingListBinding
    private lateinit var adapter: RecordingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecordingAdapter(emptyList(), onDeleteClicked = {

        })

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

//        lifecycleScope.launch {
//            val recordings = db.recordingDao().getAll()
//            // update RecyclerView or UI
//            adapter.submitList(recordings)
//        }


    }
    private val _recordings = MutableLiveData<List<Recording>>()
    val recordings: LiveData<List<Recording>> get() = _recordings

    fun loadRecordings() {
        lifecycleScope.launch {
//            _recordings.value = dao.getAll()
        }
    }
}
