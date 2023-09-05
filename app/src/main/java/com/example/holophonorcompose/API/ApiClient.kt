package com.example.holophonorcompose.API

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File

class ApiClient {
    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://your.base.url/") // Replace with your API base URL
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun uploadAudioFile(audioFile: File): Call<ResponseBody> {
        val audioRequestBody = RequestBody.create(MediaType.parse("audio/*"), audioFile)
        val audioPart = MultipartBody.Part.createFormData("audio", "audio.mp3", audioRequestBody)

        return apiService.uploadAudioFile(audioPart)
    }

    fun generateAudioFromText(text: String): Call<ResponseBody> {
        val textRequestBody = RequestBody.create(MediaType.parse("text/plain"), text)
        val textPart = MultipartBody.Part.createFormData("text", "text.txt", textRequestBody)

        return apiService.generateAudioFromText(textPart)
    }
}
