package com.example.holophonorcompose.API

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("http://20.62.248.234:5000//generateVideo")
    fun generateVideo(@Part audio: MultipartBody.Part): Call<ResponseBody>

    @Multipart
    @POST("http://20.62.248.234:5000//generateAudio")
    fun generateAudioFromText(@Part text: MultipartBody.Part): Call<ResponseBody>
}
