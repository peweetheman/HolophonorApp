package roc.ventures.holophonorCompose.recording

import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class AndroidAudioPlayer(
    private val context: Context
) {

    private var player: MediaPlayer? = null
    var isAudioPlaying: MutableState<Boolean> = mutableStateOf(false)

    fun playUri(uri : Uri) {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, uri)
            prepare()
            start()
        }
    }

    fun playFile(file: File) {
        isAudioPlaying.value = true
        MediaPlayer.create(context, file.toUri()).apply {
            player = this
            start()
        }
    }

    fun stop() {
        isAudioPlaying.value = false
        player?.stop()
        player?.release()
        player = null
    }
}