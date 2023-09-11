package roc.ventures.holophonorCompose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import roc.ventures.holophonorCompose.recording.AndroidAudioPlayer
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import roc.ventures.holophonorCompose.API.ApiClient
import roc.ventures.holophonorCompose.ui.theme.BackgroundImage
import roc.ventures.holophonorCompose.ui.theme.HolophonorTheme
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import roc.ventures.holophonorCompose.GenerateSongActivity
import roc.ventures.holophonorCompose.R

class MainActivity : ComponentActivity() {
    private final var TAG = "MainActivity"

    private var mInterstitialAd: InterstitialAd? = null
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

        var adRequest = AdRequest.Builder().build()
//getString(R.string.interstitial_ad_unit_id) for adUnitId
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError.toString().let { Log.d(TAG, it) }
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

//            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
//                // Called when ad fails to show.
//                Log.e(TAG, "Ad failed to show fullscreen content.")
//                mInterstitialAd = null
//            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }

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
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape){
            VideoPlayer(fileModel)
            return
        }

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

                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@MainActivity)
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.")
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

        // FOR TESTING TO SEE URI OF FILE
        // Text(fileModel.videoFileUri.toString())

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
                    setFullscreenButtonClickListener { isFullScreen ->
                        with(context) {
                            if (isFullScreen) {
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                            } else {
                                setScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            }
                        }
                    }
                }            },
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
    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else              -> null
    }

    private fun Context.setScreenOrientation(orientation: Int) {
        val activity = this.findActivity() ?: return
        activity.requestedOrientation = orientation
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            hideSystemUi()
        } else {
            showSystemUi()
        }
    }

    private fun Context.hideSystemUi() {
        val activity = this.findActivity() ?: return
        val window = activity.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun Context.showSystemUi() {
        val activity = this.findActivity() ?: return
        val window = activity.window ?: return
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())
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

