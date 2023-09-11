package roc.ventures.holophonorCompose.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.security.cert.CertificateFactory
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getUnsafeOkHttpClient(): OkHttpClient {
    try {
        // Create a trust manager that trusts all certificates
        val trustAllCertificates = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        })

        // Create an SSL context that uses the trust manager above
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCertificates, java.security.SecureRandom())

        // Create a hostname verifier that bypasses all hostname checks
        val hostnameVerifier = HostnameVerifier { _, _ -> true }

        // Create an OkHttpClient with the above SSL context and hostname verifier
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory)
            .hostnameVerifier(hostnameVerifier)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) // Add logging for debugging\            .connectTimeout(30, TimeUnit.SECONDS) // Set the connection timeout to 30 seconds
            .connectTimeout(3, TimeUnit.HOURS) // Set the connection timeout to 30 seconds
            .readTimeout(3, TimeUnit.HOURS)    // Set the read timeout to 30 seconds
            .build()
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

data class GenerateAudioRequest(val text: String)

class ApiClient() {
    private val apiService: ApiService

    init {
        val gson: Gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://20.62.248.234:6000") // Replace with your API base URL
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun generateVideoFromAudio(audioFile: File): Call<ResponseBody> {
        val audioRequestBody = RequestBody.create(MediaType.parse("audio/*"), audioFile)
        val audioPart = MultipartBody.Part.createFormData("audio_file", "audio.mp3", audioRequestBody)

        return apiService.generateVideo(audioPart)
    }

    fun generateAudioFromText(text: String): Call<ResponseBody> {
        val request = GenerateAudioRequest(text)
        return apiService.generateAudioFromText(request)
    }
}
