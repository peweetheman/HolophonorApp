package com.example.holophonorcompose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.*
import com.example.holophonorcompose.recording.AndroidAudioPlayer

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Pause  // ok
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import com.example.holophonorcompose.API.ApiClient
import com.example.holophonorcompose.ui.theme.BackgroundImage

import com.example.holophonorcompose.ui.theme.HolophonorTheme
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Process the selected audio file (uri)
            }
        }

    private lateinit var audioFile: File

    private val audioPlayer by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val resourceId = R.raw.sample15s // Replace "audio" with the actual resource name
            audioFile = File(context.cacheDir, "audio.mp3") // Change the file name as needed
            val inputStream = context.resources.openRawResource(resourceId)
            val outputStream = FileOutputStream(audioFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            HolophonorTheme {
                BackgroundImage() {
                    MainScreen(context)
                }
            }
        }
    }


    @Composable
    fun MainScreen(context: Context) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, GenerateSongActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Generate Song", textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { filePickerLauncher.launch("audio/*") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Upload Song", textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(context, RecordActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Record", textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            )
            {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(0.8f) // Make the Box fill the available space
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.waveform11),
                        contentDescription = "Image",
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = audioFile.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.Center) // Center the text within the Box
                            .offset(y = -15.dp)
                    )
                }

                var isAudioPlaying by remember { mutableStateOf(false) }
                IconButton(
                    onClick = {
                        if (isAudioPlaying) {
                            audioPlayer.stop()
                        } else {
                            audioPlayer.playFile(audioFile)
                        }
                        isAudioPlaying = !isAudioPlaying
                    },
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = 40.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = if (isAudioPlaying) "Pause" else "Play",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Button(
                onClick = { generateVideo(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Create AI Generated Video", color = Color.White, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            VideoPlayer(videoUri = Uri.parse("https://download.samplelib.com/mp4/sample-10s.mp4"))
        }
    }

    fun generateVideo(context: Context) {
        val apiClient = ApiClient()
        Log.d("info", "called endpoint to generate video")

        val callUpload = apiClient.generateVideoFromAudio(audioFile)
        callUpload.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("info", "SUCCESS NO WAY HOMIE")

                    // Handle the successful response, which should contain the generated video data
                    val responseBody = response.body()
                    val generatedVideoData = responseBody?.bytes()

                    // Save the received audio data to a file or process it as needed
                } else {
                    val responseBody = response.body()
                    Log.d("info", "FAILURE: " + responseBody.toString())
                    Log.d("info", responseBody.toString())
                    // Handle the API error response for generating audio
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle network or other failures
            }

        })
    }

    @Composable
    fun VideoPlayer(videoUri: Uri) {
        val context = LocalContext.current
        val exoPlayer = remember {
            SimpleExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
            }
        }

        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (exoPlayer.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }


    }
    @Preview(showBackground = true)
    @Composable
    fun MainScreenPreview() {
        HolophonorTheme {
            val context = LocalContext.current
            MainScreen(context)
        }
    }
}

