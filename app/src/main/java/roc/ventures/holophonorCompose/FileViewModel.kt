package roc.ventures.holophonorCompose

import android.content.ContentResolver
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import roc.ventures.holophonorCompose.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileViewModel(private val context: Context) : ViewModel() {
    val audioFileState: MutableState<File> = mutableStateOf(getInitialFile())

    val videoFileUri: MutableState<Uri> =
        mutableStateOf(Uri.parse("https://download.samplelib.com/mp4/sample-10s.mp4"))

    fun updateVideoUri(newUri: Uri) {
        videoFileUri.value = newUri
    }

    fun updateFile(newFile: File) {
        audioFileState.value = newFile
    }

    fun loadAudioFileFromUri(audioUri: Uri): File? {
        val contentResolver: ContentResolver = context.contentResolver
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(audioUri)
            if (inputStream != null) {
                val cacheDir = context.cacheDir
                val tempFile = File.createTempFile("audio", ".mp3", cacheDir)
                val outputStream = FileOutputStream(tempFile)

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                outputStream.close()

                return tempFile
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun getInitialFile(): File {
        // Initialize with the resource file
        val resourceFile = File(context.filesDir, "mysong.mp3")
        if (!resourceFile.exists()) {
            // Copy the resource file to the cache directory if it doesn't exist
            val cacheFile = File(context.cacheDir, "mysong.mp3")
            cacheFile.outputStream().use { outputStream ->
                context.resources.openRawResource(R.raw.sample15s).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return cacheFile
        }
        return resourceFile
    }
}
