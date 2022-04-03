package com.example.group_d.data

// this class contains constants used in messaging / notifications
class ConstantsClass {
    companion object{
        // the URL used to message firebase
        const val BASE_URL = "https://fcm.googleapis.com"
        // the Key that is required to access the firestore-database
        const val SERVER_KEY = "AAAACHmFr9o:APA91bFCecvjtkyNcDzK0Mwgoqc7LUhMeBfmIC-qSYVRbUs5zrfcOyyjxdGW177v7SE2ycsbHHDgbHesJXWEdcceiyfwCuKmLCZEMiDQipxBcjRwVEBLonVTmpQo8VbB6uZxWWzhAaDa"
        // the content-type of the sent messages
        const val CONTENT_TYPE = "application/json"
    }
}