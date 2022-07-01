package com.example.app.musicapp.adapter

import android.R
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.app.musicapp.databinding.PlaylistViewBinding
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class PlaylistAdapter(
    private val items: ClipData?,
    private val context: Context,
    private val listener: (ClipData.Item?) -> Unit

) :
RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val notificationBinding =
            PlaylistViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(notificationBinding, parent)
    }

    override fun getItemCount() = items!!.itemCount

    class ViewHolder(val view: PlaylistViewBinding, val parent: ViewGroup) :
        RecyclerView.ViewHolder(view.root) {
        private var mediaPlayer: MediaPlayer? = null
        lateinit var timer: ScheduledExecutorService
        var duration: String? = null

        fun bindTo(notification: ClipData.Item,context: Context, listener: View.OnClickListener) {

            view.audioTv.setText(getNameFromUri(notification.uri,context))
            createMediaPlayer(notification.uri,context)

            view.root.setOnClickListener {

                if (mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.pause()
                        view.playBtn2.visibility = View.GONE
                        view.playBtn.visibility = View.VISIBLE

                        timer.shutdown()
                    }
                    else {
                        mediaPlayer!!.start()
                        view.playBtn2.visibility = View.VISIBLE
                        view.playBtn.visibility = View.GONE


                        timer = Executors.newScheduledThreadPool(1)
                        timer.scheduleAtFixedRate({
                            if (mediaPlayer != null) {
                                if (!view.seekbar1.isPressed()) {
                                    view.seekbar1.setProgress(mediaPlayer!!.currentPosition)
                                }
                            }
                        }, 10, 10, TimeUnit.MILLISECONDS)
                    }
                }

            }



        }

        private fun createMediaPlayer(uri: Uri?, context: Context) {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                mediaPlayer!!.setDataSource(context, uri!!)
                mediaPlayer!!.prepare()
                view.audioTv.setText(getNameFromUri(uri,context))
                view.playBtn.setEnabled(true)
                val millis = mediaPlayer!!.duration
                val total_secs = TimeUnit.SECONDS.convert(millis.toLong(), TimeUnit.MILLISECONDS)
                val mins = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
                val secs = total_secs - mins * 60
                duration = "$mins:$secs"
                view.durationTv.setText("$duration")
                view.seekbar1.setMax(millis)
                view.seekbar1.setProgress(0)
                mediaPlayer!!.setOnCompletionListener { releaseMediaPlayer() }
            } catch (e: IOException) {
                view.audioTv.setText(e.toString())
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
            if (timer != null) {
                timer.shutdown()
            }
            if (mediaPlayer != null) {
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            view.playBtn.setEnabled(false)

            view.durationTv.setText("00:00")
            view.seekbar1.setMax(100)
            view.seekbar1.setProgress(0)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(items!!.getItemAt(position),context) { listener(items!!.getItemAt(position)) }
    }

}