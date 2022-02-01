package com.example.group_d.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

data class Game(
    val gameID: String,
    var gameData: List<Long>,
    val gameType: String,
    val players: List<User>
)
