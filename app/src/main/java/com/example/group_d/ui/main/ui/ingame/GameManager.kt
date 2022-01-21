package com.example.group_d.ui.main.ui.ingame

import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.tictactoe.Field
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GameManager {

    private val db = Firebase.firestore

    fun createGameFromChallenge(challenge: Challenge): String {
        // TODO
        return "42"
    }

    fun loadGameFromServer(gameID: String): Game {
        val gameDoc = db.collection("games").document(gameID)
        TODO("Not implemented")
    }
}