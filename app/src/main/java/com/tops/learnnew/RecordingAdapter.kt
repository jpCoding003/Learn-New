package com.tops.learnnew

import Recording
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tops.learnnew.databinding.ItemRecordingBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordingAdapter(
    private var recordings: List<Recording>,
    private val onDeleteClicked: (Recording) -> Unit
) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null

    inner class ViewHolder(val binding: ItemRecordingBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(newList: List<Recording>) {
        recordings = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = recordings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rec = recordings[position]
        holder.binding.fileName.text = File(rec.filePath).name
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        holder.binding.timestamp.text = sdf.format(Date(rec.timestamp))

        holder.binding.playBtn.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                holder.binding.playBtn.text = "▶"
            } else {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(rec.filePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        holder.binding.playBtn.text = "▶"
                    }
                }
                holder.binding.playBtn.text = "⏸"
            }
        }

        holder.binding.deleteBtn.setOnClickListener {
            // call host to delete from DB
            onDeleteClicked(rec)
        }
    }
}
