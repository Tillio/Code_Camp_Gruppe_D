package com.example.group_d.data

// this class contains the layout of the push-notifications
data class PushNotification(
    // data contains the message
    val data: NotificationData,
    // to contains the id of the reciever of the message
    val to: String
)