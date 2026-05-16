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

    private val authenticator = okhttp3.Authenticator { _, response ->
        val refreshToken = sessionManager?.getRefreshToken() ?: return@Authenticator null

        // Avoid multiple refreshes
        if (response.request.header("Authorization") != "Bearer ${sessionManager?.getToken()}") {
            return@Authenticator null
        }

        synchronized(this) {
            val newToken = sessionManager?.getToken()
            // Check if it was already refreshed by another thread
            if (response.request.header("Authorization") != "Bearer $newToken") {
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }

            // Sync refresh call
            val refreshResponse = kotlinx.coroutines.runBlocking {
                val repository = AuthRepository(authApi)
                repository.refreshSilent(refreshToken)
            }

            return@synchronized refreshResponse.fold(
                onSuccess = { auth ->
                    if (auth?.token != null) {
                        sessionManager?.saveSession(
                            auth.token,
                            auth.refreshToken,
                            auth.user?.id ?: -1L,
                            auth.user?.firstName ?: "",
                            auth.user?.lastName ?: "",
                            auth.user?.email ?: "",
                            auth.user?.primaryRole ?: ""
                        )
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${auth.token}")
                            .build()
                    } else {
                        sessionManager?.clearSession()
                        null
                    }
                },
                onFailure = {
                    sessionManager?.clearSession()
                    null
                }
            )
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(authenticator)
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
