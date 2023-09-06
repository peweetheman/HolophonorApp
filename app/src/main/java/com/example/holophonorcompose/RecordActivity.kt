package com.example.holophonorcompose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.holophonorcompose.recording.AndroidAudioPlayer
import com.example.holophonorcompose.recording.AndroidAudioRecorder
import com.example.holophonorcompose.ui.theme.BackgroundImage
import com.example.holophonorcompose.ui.theme.HolophonorTheme

import java.io.File

class RecordActivity : ComponentActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )
        setContent {
            HolophonorTheme {
                BackgroundImage() {
                    RecordScreen()
                }
            }
        }
    }

    @Composable
    fun RecordScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {

                File(cacheDir, "audio.mp3").also {
                    recorder.start(it)
                    audioFile = it
                }
            }) {
                Text(text = "Start recording")
            }
            Button(onClick = {
                recorder.stop()
            }) {
                Text(text = "Stop recording")
            }
            Button(onClick = {
                player.playFile(audioFile ?: return@Button)
            }) {
                Text(text = "Play")
            }
            Button(onClick = {
                player.stop()
            }) {
                Text(text = "Stop playing")
            }
        }
    }
}