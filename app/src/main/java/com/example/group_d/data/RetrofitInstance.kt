package com.example.group_d.data

import com.example.group_d.BuildConfig
import com.example.group_d.apis.NotificationAPI
import com.example.group_d.data.ConstantsClass.Companion.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// used to send messages to firebase
class RetrofitInstance {
    companion object {
         private val retrofit by lazy {
             Retrofit.Builder()
                 .baseUrl(BASE_URL)
                 .addConverterFactory(GsonConverterFactory.create())
                 .client(initOkHttp())
                 .build()
         }
        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
        private fun initOkHttp(): OkHttpClient {
            val client = OkHttpClient.Builder()

            client.connectTimeout(15, TimeUnit.SECONDS)
            client.readTimeout(15, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                client.addInterceptor(loggingInterceptor)
            }

            return client.build()
        }
    }
}