package com.example.group_d.data.model

import com.google.firebase.firestore.DocumentReference

data class Game(
    val gameID: String,
    var gameData: MutableList<Long>,
    val gameType: String,
    val players: List<DocumentReference>
)
