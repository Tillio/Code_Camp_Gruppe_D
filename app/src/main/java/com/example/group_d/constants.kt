package com.example.group_d

//collection names
const val COL_USER = "user"
const val COL_GAMES = "games"

//user keywords
const val USER_CHALLENGES = "challenges"
const val USER_FRIEND_REQUESTS = "friendRequests"
const val USER_FRIENDS = "friends"
const val USER_GAMES = "games"
const val USER_STATUS = "status"
const val USER_SEARCHING = "searching"
const val USER_NAME = "name"
const val USER_DATA = "userData"

//games keywords
const val GAME_BEGINNER = "beginner"
const val GAME_DATA = "gameData"
const val GAME_TYPE = "gameType"
const val GAME_TYPE_TIC_TAC_TOE = "TIC_TAC_TOE"
const val GAME_TYPE_MENTAL_ARITHMETICS = "Mental Arithmetics"
const val GAME_TYPE_STEPS_GAME = "Steps Game"
const val GAME_PLAYERS = "players"
const val STEPS_TO_DO = "15"

val GAME_TYPE_MAP = hashMapOf(
    GAME_TYPE_TIC_TAC_TOE to "Tic Tac Toe",
    GAME_TYPE_MENTAL_ARITHMETICS to "Kopfrechnen",
    GAME_TYPE_STEPS_GAME to "Steps"
)