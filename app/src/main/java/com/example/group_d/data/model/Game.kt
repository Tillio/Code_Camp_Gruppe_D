package com.example.group_d.data.model

data class Game(
    val gameID: String,
    val gameData: List<Int>,
    val gameType: String,
    val players: List<User>
)
