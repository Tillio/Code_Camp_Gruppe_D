package com.example.group_d.data.handler

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.group_d.R

class NotificationHandler {
    val str_channelID = "CC_GD_C_ID"
    val str_channelName = "CC_GD_C_N"
    lateinit var mainActivity: Activity

    public fun createNotificationChannel(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            mainActivity = activity
            val channel = NotificationChannel(str_channelID, str_channelName, NotificationManager.IMPORTANCE_DEFAULT)
            val manager: NotificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    public fun sendNotification(text: String){
        val notification = NotificationCompat.Builder(mainActivity, str_channelID)
            .setContentTitle("Prototype_D")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationManager = NotificationManagerCompat.from(mainActivity)
        notificationManager.notify(0, notification)
    }
}