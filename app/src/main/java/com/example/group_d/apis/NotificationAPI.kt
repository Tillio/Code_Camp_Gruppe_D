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
    // The Headers contain the authorization-key for the database, and the type off the send content
    @Headers("Authorization: key=$SERVER_KEY", "Content-Type: $CONTENT_TYPE")
    // the POST is added to the Address, to which the Notification is sent
    @POST("fcm/send")
    // Defines the layout of the notification
    suspend fun postNotification(
        // Inserts the body from the "PushNotification"-class
        @Body notification: PushNotification
    ): Response<ResponseBody>
}