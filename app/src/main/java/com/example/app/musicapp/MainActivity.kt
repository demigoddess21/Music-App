package com.example.app.musicapp

import android.R.id.button2
import android.content.Intent
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.musicapp.adapter.PlaylistAdapter
import com.example.app.musicapp.databinding.ActivityMainBinding
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val REQ_CODE_PICK_SOUNDFILE: Int = 111

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.black)
        }
        binding.selectAudioBtn.setOnClickListener {
            val intent: Intent
            intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            intent.type = "audio/mpeg"
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Select Audio"
                ), REQ_CODE_PICK_SOUNDFILE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == RESULT_OK) {
            if (data != null && data.clipData != null) {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                binding.welcomeTv.visibility = View.GONE
                binding.selectAudioBtn.visibility = View.GONE
                binding.rvPlaylist.visibility = View.VISIBLE

                binding.rvPlaylist.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

                binding.rvPlaylist.adapter =
                    PlaylistAdapter(data.clipData, this, mediaPlayer) { item, index ->

                    }
            }


        }
    }


}