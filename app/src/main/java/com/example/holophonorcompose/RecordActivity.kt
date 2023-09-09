package com.example.holophonorcompose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
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
                    RecordScreen(LocalContext.current)
                }
            }
        }
    }

    @Composable
    fun RecordScreen(context : Context) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // RECORDER BUTTON
            Button(onClick = {
                if (recorder.isRecording.value) {
                    recorder.stop()
                } else {
                    File(cacheDir, "audio.mp3").also {
                        recorder.start(it)
                        audioFile = it
                    }
                }
            }) {
                if (recorder.isRecording.value) {
                    Text(text = "Stop recording")
                } else {
                    Text(text = "Start recording")
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                // RESET BUTTON


                // PLAY/PAUSE RECORDING BUTTON
                Button(onClick = {
                    if (player.isAudioPlaying.value){
                        player.stop()
                    } else {
                        player.playFile(audioFile ?: return@Button)
                    }
                }) {
                    if (player.isAudioPlaying.value) {
                        Text(text = "Stop Audio")
                    } else {
                        Text(text = "Play Recording")
                    }
                }

                // SUBMIT BUTTON
                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("audioFileUri", audioFile?.toUri().toString())
                        context.startActivity(intent)
                    }
                ) {
                    Text(text = "Finish")
                }


            }
        }
    }
}