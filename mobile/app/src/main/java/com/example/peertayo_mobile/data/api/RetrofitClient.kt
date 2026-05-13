package com.example.peertayo_mobile.data.api

import android.content.Context
import com.example.peertayo_mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Corrected BASE_URL for Spring Boot with /api/v1/ prefix
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var sessionManager: SessionManager? = null

    fun init(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = sessionManager?.getToken()
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val evaluationApi: EvaluationApi by lazy {
        retrofit.create(EvaluationApi::class.java)
    }
}
