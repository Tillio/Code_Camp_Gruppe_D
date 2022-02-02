package com.example.group_d.ui.main.ui.ingame

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class GameViewModel : ViewModel() {

    private val db = Firebase.firestore

    val runGame = MutableLiveData<Game>()
    protected val runGameRaw: Game get() = runGame.value!!

    abstract fun initGame(snap: DocumentSnapshot, gameID: String)

    abstract fun onGameDataChanged(gameData: List<Long>)

    fun loadRunningGame(gameID: String) {
        val docref = db.collection(COL_GAMES).document(gameID)
        docref.get().addOnSuccessListener {
            initGame(it, gameID)
            docref.addSnapshotListener { value, error ->
                if (value == null || !value.exists()) {
                    Log.d(null, "Error:")
                    error?.printStackTrace()
                    return@addSnapshotListener
                }

                val gameData = value[GAME_DATA] as MutableList<Long>
                runGame.value!!.gameData = gameData
                onGameDataChanged(gameData)
            }
        }
    }

    fun updateGameData() {
        val docref = db.collection(COL_GAMES).document(runGameRaw.gameID)
        docref.update(GAME_DATA, runGameRaw.gameData)
    }

    fun getOwnUserID(): String {
        val ownUid = Firebase.auth.currentUser!!.uid
        return ownUid
    }
}