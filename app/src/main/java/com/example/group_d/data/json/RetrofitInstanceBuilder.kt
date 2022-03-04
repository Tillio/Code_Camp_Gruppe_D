package com.example.group_d.data.json

import com.example.group_d.LOCATIONS_BASE_URL
import com.google.gson.GsonBuilder
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type


class RetrofitInstanceBuilder {
    companion object {
        private fun createGsonConverter(type: Type, typeAdapter: Any): Converter.Factory {
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(type, typeAdapter)
            val gson = gsonBuilder.create()
            return GsonConverterFactory.create(gson)
        }

        fun getRetrofitInstance(type: Type, typeAdapter: Any): Retrofit {
            return Retrofit.Builder()
                .baseUrl(LOCATIONS_BASE_URL)
                .addConverterFactory(createGsonConverter(type, typeAdapter))
                .build()
        }
    }
}