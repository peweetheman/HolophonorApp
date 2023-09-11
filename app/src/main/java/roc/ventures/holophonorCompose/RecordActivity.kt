package roc.ventures.holophonorCompose

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import roc.ventures.holophonorCompose.recording.AndroidAudioPlayer
import roc.ventures.holophonorCompose.recording.AndroidAudioRecorder
import roc.ventures.holophonorCompose.ui.theme.BackgroundImage
import roc.ventures.holophonorCompose.ui.theme.HolophonorTheme

import java.io.File

class RecordActivity : ComponentActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private lateinit  var player : AndroidAudioPlayer

    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        player = AndroidAudioPlayer(applicationContext)
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
            Button(
                modifier = Modifier.size(90.dp),
                        onClick = {
                if (recorder.isRecording.value) {
                    recorder.stop()
                } else {
                    File(cacheDir, "audio.mp3").also {
                        recorder.start(it)
                        audioFile = it
                    }
                }
            }) {
                Icon(
                    imageVector = if (recorder.isRecording.value) Icons.Default.Pause else Icons.Default.Mic,
                    contentDescription = if (recorder.isRecording.value) "Pause" else "Record",
                    tint = Color.White,
                    modifier = Modifier.size(90.dp)
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ){

                // PLAY/PAUSE RECORDING BUTTON
                Button(onClick = {
                    if (player.isAudioPlaying.value){
                        player.stop()
                    } else {
                        player.playFile(audioFile ?: return@Button)
                    }
                }) {
                    if (player.isAudioPlaying.value) {
                        Text(text = "Pause")
                    } else {
                        Text(text = "Play Recording")
                    }
                }

                // RESET RECORDING BUTTON
                Button(onClick = {
                    player = AndroidAudioPlayer(applicationContext)
                }) {
                    Text(text = "Reset")
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