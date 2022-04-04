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
    val str_channelID = "CC_GD_C"
    val str_channelName = "CC_GD_C_N"
    lateinit var mainActivity: Activity

    // creates the notification-channel
    public fun createNotificationChannel(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // specifies the activity
            mainActivity = activity
            // sets up the channel
            val channel = NotificationChannel(str_channelID, str_channelName, NotificationManager.IMPORTANCE_DEFAULT)
            // sets up the manager
            val manager: NotificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // creates the channel
            manager.createNotificationChannel(channel)
        }
    }
}

// sends the Notification
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {
    // sets up the builder, passes the context, and title
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.default_notification_channel_id)
    )
        // then selects the icon of the Notification,
        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
        // the title of the notification
        .setContentTitle("CC_GD_C")
        // then sets the text of the notification
        .setContentText(messageBody)
        // and finally the priority
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    // builds and sends the notification
    notify(0, builder.build())
}