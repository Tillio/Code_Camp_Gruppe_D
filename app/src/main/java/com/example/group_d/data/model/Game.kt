package com.example.group_d.data.model

data class Game(
    val gameID: String,
    val gameData: Map<Any, Any>,
    val gameType: String,
    val players: List<User>
)
