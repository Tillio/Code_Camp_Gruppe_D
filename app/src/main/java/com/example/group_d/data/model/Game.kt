package com.example.group_d.data.model

import android.os.Parcelable
import com.example.group_d.GAME_DRAW
import com.google.firebase.firestore.DocumentReference



data class Game(
    var beginner: String,
    var gameData: MutableList<String>,
    val gameType: String,
    val players: List<DocumentReference>,


) {
    var id: String = ""
    var completionDate: Long = 0
    var winner: String = GAME_DRAW
}
