package com.thefarhany.eventapp.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.thefarhany.eventapp.utils.SessionManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8899/api/v1/"
    private const val TAG = "RetrofitClient"

    private var sessionManager: SessionManager? = null
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        sessionManager = SessionManager(context)
    }

    /**
     * ✅ Persistent CookieJar that saves cookies to SharedPreferences
     */
    private val cookieJar = object : CookieJar {
        private val prefs: SharedPreferences by lazy {
            context!!.getSharedPreferences("http_cookies", Context.MODE_PRIVATE)
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.forEach { cookie ->
                if (cookie.name == "jwt") {
                    // Save cookie to SharedPreferences
                    prefs.edit()
                        .putString("jwt_cookie", cookie.value)
                        .putLong("jwt_expires", cookie.expiresAt)
                        .apply()

                    // Also save to SessionManager
                    sessionManager?.saveAuthToken(cookie.value)

                    Log.d(TAG, "✅ Cookie saved: ${cookie.value.take(20)}...")
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            // Try to load cookie from SharedPreferences
            val cookieValue = prefs.getString("jwt_cookie", null)
            val expiresAt = prefs.getLong("jwt_expires", 0)

            if (cookieValue != null && expiresAt > System.currentTimeMillis()) {
                val cookie = Cookie.Builder()
                    .name("jwt")
                    .value(cookieValue)
                    .domain(url.host)
                    .path("/")
                    .expiresAt(expiresAt)
                    .build()

                Log.d(TAG, "✅ Cookie loaded for request: ${cookieValue.take(20)}...")
                return listOf(cookie)
            } else if (cookieValue != null) {
                // Cookie expired, clear it
                Log.d(TAG, "⚠️ Cookie expired, clearing...")
                prefs.edit().clear().apply()
                sessionManager?.clearSession()
            }

            return emptyList()
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Also add Authorization header as backup
        val token = sessionManager?.getAuthToken()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        requestBuilder
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * ✅ Clear cookies (call this on logout)
     */
    fun clearCookies() {
        context?.getSharedPreferences("http_cookies", Context.MODE_PRIVATE)
            ?.edit()
            ?.clear()
            ?.apply()
        Log.d(TAG, "🧹 Cookies cleared")
    }
}
