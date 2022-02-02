package com.example.group_d.data.model

data class Game(
    val gameID: String,
    var gameData: MutableList<Long>,
    val gameType: String,
    val players: List<User>
)
