package com.example.group_d.ui.main.ui.ingame

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.group_d.COL_GAMES
import com.example.group_d.data.model.Game
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class GameViewModel : ViewModel() {

    val db = Firebase.firestore
    lateinit var runGame: Game

    fun loadRunningGame(gameID: String) {
        val docref = db.collection(COL_GAMES).document(gameID)
        docref.addSnapshotListener { value, error ->
            if (value == null || !value.exists()) {
                Log.d(null, "Error:")
                error?.printStackTrace()
                return@addSnapshotListener
            }

            val gameData = value["gameData"]
            runGame.gameData.value = gameData as List<Long>
            TODO("Not implemented")
        }
    }
}