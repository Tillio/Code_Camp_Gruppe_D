package com.example.group_d.apis

import com.example.group_d.data.ConstantsClass.Companion.CONTENT_TYPE
import com.example.group_d.data.ConstantsClass.Companion.SERVER_KEY
import com.example.group_d.data.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type: $CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}