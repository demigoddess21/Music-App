package com.example.app.musicapp.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app.musicapp.R
import com.example.app.musicapp.databinding.PlaylistViewBinding
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class PlaylistAdapter(
    private val items: ClipData?,
    private val context: Context,
    private val mediaPlayer: MediaPlayer,
    private val listener: (ClipData.Item?, index: Int) -> Unit
) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private var progressHandler = Handler(Looper.getMainLooper())

    private var progressRunnable: Runnable? = null

    private var currentPlayingIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val notificationBinding =
            PlaylistViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(notificationBinding, parent)
    }

    override fun getItemCount() = items!!.itemCount

    inner class ViewHolder(val view: PlaylistViewBinding, val parent: ViewGroup) :
        RecyclerView.ViewHolder(view.root) {
        var duration: String? = null

        fun bindTo(notification: ClipData.Item, context: Context) {

            view.audioTv.setText(getNameFromUri(notification.uri, context))

            view.root.setOnClickListener {

                if (currentPlayingIndex == adapterPosition) {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        view.playBtn.visibility = View.VISIBLE
                        view.playBtn2.visibility =View.GONE
                        stopProgressHandler()
                    } else {
                        mediaPlayer.start()
                        view.playBtn.visibility = View.GONE
                        view.playBtn2.visibility =View.VISIBLE
                        startProgressHandler(view)
                    }
                } else {
                    listener(notification, adapterPosition)
                    currentPlayingIndex = adapterPosition
                    startPlayingSong(context = view.root.context, uri = notification.uri)
                }

            }


        }

        private fun startPlayingSong(uri: Uri?, context: Context) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, uri!!)
                mediaPlayer.prepare()
                mediaPlayer.start()
                view.playBtn.visibility = View.GONE
                view.playBtn2.visibility =View.VISIBLE
                startProgressHandler(view)
                view.audioTv.text = getNameFromUri(uri, context)
                view.playBtn.isEnabled = true
                val millis = mediaPlayer.duration
                val total_secs = TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
                val mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
                val secs = total_secs - mins * 60
                duration = "$mins:$secs"
                view.durationTv.text = "$duration"
                view.seekbar1.max = millis
                view.seekbar1.progress = 0
                mediaPlayer.setOnCompletionListener { releaseMediaPlayer() }
            } catch (e: IOException) {
                view.audioTv.text = e.toString()
            }
        }

        @SuppressLint("Range")
        fun getNameFromUri(uri: Uri?, context: Context): String? {
            var fileName = ""
            var cursor: Cursor? = null
            cursor = context.contentResolver.query(
                uri!!, arrayOf(
                    MediaStore.Images.ImageColumns.DISPLAY_NAME
                ), null, null, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                fileName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
            }
            if (cursor != null) {
                cursor.close()
            }
            return fileName
        }

        fun releaseMediaPlayer() {
            mediaPlayer.reset()
            stopProgressHandler()
            view.playBtn.isEnabled = false
            view.durationTv.text = "00:00"
            view.seekbar1.max = 100
            view.seekbar1.progress = 0
        }
    }

    fun stopProgressHandler() {
        progressRunnable?.let {
            progressHandler.removeCallbacks(it)
        }
    }

    fun startProgressHandler(binding: PlaylistViewBinding) {
        progressRunnable?.let { progressHandler.removeCallbacks(it) }
        progressRunnable = object : Runnable {
            override fun run() {
                if (!binding.seekbar1.isPressed) {
                    binding.seekbar1.progress = mediaPlayer.currentPosition
                }
                progressHandler.postDelayed(this, 500)
            }
        }
        progressHandler.post(progressRunnable!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(
            items!!.getItemAt(position),
            context
        )
    }

}