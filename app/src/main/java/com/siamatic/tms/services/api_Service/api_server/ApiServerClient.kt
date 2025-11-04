package com.siamatic.tms.services.api_Service.api_server

import android.content.Context
import com.siamatic.tms.constants.DEVICE_API_TOKEN
import com.siamatic.tms.util.sharedPreferencesClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object ApiServerClient {
  private const val BASE_URL = "https://tms.siamatic.co.th/smtrack/"
  private var token: String? = null

  fun setToken(context: Context) {
    token = sharedPreferencesClass(context).getPreference(DEVICE_API_TOKEN, "String", "").toString()
  }

  private fun getHttpClient(): OkHttpClient {
    return try {
      val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
      })

      val sslContext = SSLContext.getInstance("SSL")
      sslContext.init(null, trustAllCerts, java.security.SecureRandom())

      OkHttpClient.Builder()
        .addInterceptor { chain ->
          val original = chain.request()
          val newRequest = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .build()
          chain.proceed(newRequest)
        }
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier {_, _ -> true}
        .addInterceptor(HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  // สำหรับ Production - มีการใช้ SSL Certificate
  private fun getSafeOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
      })
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(getHttpClient())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
  val apiServerService: ApiServerService = retrofit.create(ApiServerService::class.java)
}