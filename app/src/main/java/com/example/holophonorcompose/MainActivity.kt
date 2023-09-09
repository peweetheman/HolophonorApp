package com.example.holophonorcompose

import FileViewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.example.holophonorcompose.API.ApiClient
import com.example.holophonorcompose.ui.theme.BackgroundImage

import com.example.holophonorcompose.ui.theme.HolophonorTheme
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d("log info", uri.path.toString())
                val file: File? = fileModel.loadAudioFileFromUri(uri)
                fileModel.updateFile(file!!)
                Log.d("log info", fileModel.audioFileState.value.name)
            }
        }

    private val audioPlayer by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private val fileModel by lazy {
        FileViewModel(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val audioFileUri = intent.getStringExtra("audioFileUri")
        if (audioFileUri != null) {
            val file: File? = fileModel.loadAudioFileFromUri(audioFileUri.toUri())
            fileModel.updateFile(file!!)
        }

        setContent {
            HolophonorTheme {
                BackgroundImage() {
                    MainScreen(LocalContext.current)
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
                        text = fileModel.audioFileState.value.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.Center) // Center the text within the Box
                            .offset(y = -15.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (audioPlayer.isAudioPlaying.value) {
                            audioPlayer.stop()
                        } else {
                            audioPlayer.playFile(fileModel.audioFileState.value)
                        }
                    },
                    modifier = Modifier
                        .size(100.dp)
                        .offset(y = 40.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (audioPlayer.isAudioPlaying.value) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = if (audioPlayer.isAudioPlaying.value) "Pause" else "Play",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Button(
                onClick = {
                    generateVideo(context) { uri ->
                        if (uri != null) {
                            Log.d("SUCCESS", "UPDATED FILE MODEL URI")
                            fileModel.updateVideoUri(uri)
                        } else {
                            Log.d("ERROR", "URI WAS NULL")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Create AI Generated Video", color = Color.White, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            VideoPlayer(fileModel)
        }
    }

    private fun generateVideo(context: Context, callback: (Uri?) -> Unit) {
        val apiClient = ApiClient()
        var uri: Uri? = null
        Log.d("info", "called endpoint to generate video")

        val callUpload = apiClient.generateVideoFromAudio(fileModel.audioFileState.value)
        callUpload.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("info", "SUCCESS NO WAY HOMIE")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        uri = saveResponseBodyToTempFile(context, responseBody)
                        callback(uri) // Update the Uri using the callback
                    }
                } else {
                    val responseBody = response.body()
                    Log.d("info", "FAILURE: " + responseBody.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle network or other failures
            }

        })
    }

    fun saveResponseBodyToTempFile(context: Context, responseBody: ResponseBody): Uri {
        val tempDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val tempFile = File(tempDir, "video.mp4")
        try {
            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush() // Add this line
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            throw e
        }
        Log.d("URI", tempFile.toURI().toString())
        return tempFile.toUri()
    }

    @Composable
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun VideoPlayer(fileModel: FileViewModel) {

        Text(fileModel.videoFileUri.toString())
        Spacer(modifier = Modifier.width(16.dp))

        val context = LocalContext.current
        val mediaItem = MediaItem.fromUri(fileModel.videoFileUri.value)

        val exoPlayer by remember { mutableStateOf(ExoPlayer.Builder(context).build()) }
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        AndroidView(
            modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                }
            },
            update = { view ->
                // View's been inflated or state read in this block has been updated
                //  AndroidView will recompose whenever the state changes of exoPlayer
                view.player = exoPlayer
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
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

