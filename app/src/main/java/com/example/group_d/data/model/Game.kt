package com.example.group_d.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference



data class Game(
    var beginner: String,
    var gameData: MutableList<String>,
    val gameType: String,
    val players: List<DocumentReference>,


) {
    var id: String = ""
    var completionDate: Long = 0
}
