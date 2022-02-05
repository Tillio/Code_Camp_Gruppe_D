package com.example.group_d.ui.main.ui.ingame

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class GameViewModel : ViewModel() {

    private val db = Firebase.firestore

    val runGame = MutableLiveData<Game>()
    protected val runGameRaw: Game get() = runGame.value!!

    lateinit var runGameID: String

    abstract fun initGame(snap: DocumentSnapshot, docref: DocumentReference)

    abstract fun onGameDataChanged(gameData: List<Long>)

    open fun onServerGameDataChanged(snap: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (snap == null || !snap.exists()) {
            Log.d(null, "Error:")
            error?.printStackTrace()
            return
        }

        val gameData = snap[GAME_DATA] as MutableList<Long>
        runGame.value!!.gameData = gameData
        onGameDataChanged(gameData)
    }

    fun loadRunningGame(gameID: String) {
        runGameID = gameID
        val docref = db.collection(COL_GAMES).document(gameID)
        docref.get().addOnSuccessListener {
            initGame(it, docref)
        }
    }

    fun updateGameData() {
        val docref = db.collection(COL_GAMES).document(runGameID)
        docref.update(GAME_DATA, runGameRaw.gameData)
    }

    fun getOwnUserID(): String {
        val ownUid = Firebase.auth.currentUser!!.uid
        return ownUid
    }
}