package com.example.holophonorcompose.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) {

    private var recorder: MediaRecorder? = null
    var isRecording: MutableState<Boolean> = mutableStateOf(false)

    private fun createRecorder(): MediaRecorder {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    fun start(outputFile: File) {
        isRecording.value = true
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    fun stop() {
        isRecording.value = false
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}