package com.example.group_d.services

import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.group_d.COL_USER
import com.example.group_d.data.handler.sendNotification
import com.example.group_d.ui.main.MainScreenActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val db = Firebase.firestore

    override fun onMessageReceived(p0: RemoteMessage) {
        p0?.data?.let {
            Log.d(TAG, "Message data payload: " + p0.data)
            //val msgStr: String = p0.data["message"].toString()
            sendNotification(p0.data["message"].toString())
        }

        p0?.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body!!)
        }
    }

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).update("token", token)
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(messageBody: String) {
        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        notificationManager.sendNotification(messageBody, applicationContext)
    }
}