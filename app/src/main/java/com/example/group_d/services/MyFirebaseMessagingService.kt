package com.example.group_d.services

import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.group_d.COL_USER
import com.example.group_d.data.handler.sendNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val db = Firebase.firestore

    // triggers Notification when receiving a message from Firebase
    override fun onMessageReceived(p0: RemoteMessage) {
        // when the message contains a data-part
        p0?.data?.let {
            // Log the data of the message (for debugging)
            Log.d(TAG, "Message data payload: " + p0.data)
            // send the Notification
            sendNotification(p0.data["message"].toString())
        }

        // when the message contains a body-part
        p0?.notification?.let {
            // Log the body of the message (for debugging)
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // send the Notification
            sendNotification(it.body!!)
        }
    }

    // when the instance of the app receives a new token
    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // stores the token in the firestore-database
        db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).update("token", token)
        // log the new Token (for debugging)
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(messageBody: String) {
        // sets up the NotificationManager
        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        // sends the Notification
        notificationManager.sendNotification(messageBody, applicationContext)
    }
}