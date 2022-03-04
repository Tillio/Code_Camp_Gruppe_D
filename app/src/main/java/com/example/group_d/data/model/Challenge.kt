package com.example.group_d.data.model

data class Challenge(
    val user: User,
    val gameType: String
){
    var step_game_time: Long = 0
}
