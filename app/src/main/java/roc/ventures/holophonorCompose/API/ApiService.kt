package roc.ventures.holophonorCompose.API

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("https://20.62.248.234:6000//generateVideo")
    fun generateVideo(@Part audio: MultipartBody.Part): Call<ResponseBody>

    @POST("https://20.62.248.234:6000//generateAudio")
    fun generateAudioFromText(@Body request: GenerateAudioRequest): Call<ResponseBody>
}
