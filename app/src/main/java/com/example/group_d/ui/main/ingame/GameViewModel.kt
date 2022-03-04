package com.example.group_d.ui.main.ingame

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class GameViewModel : ViewModel() {

    private val db = Firebase.firestore

    val runGame = MutableLiveData<Game?>()
    protected val runGameRaw: Game get() = runGame.value!!

    lateinit var runGameID: String

    private lateinit var gameDataSnapshotRegistration: ListenerRegistration

    abstract fun initGame(snap: DocumentSnapshot, docref: DocumentReference)

    open fun showEndstate(gameID: String) {
        // do nothing
    }

    abstract fun onGameDataChanged(gameData: List<String>)

    open fun onServerGameDataChanged(snap: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if (snap == null || !snap.exists()) {
            Log.d(null, "Error:")
            error?.printStackTrace()
            return
        }

        val gameData = snap[GAME_DATA] as MutableList<String>
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

    fun prepareEndstate(){
        showEndstate(runGameID)
    }

    fun addGameDataChangedListener(docref: DocumentReference) {
        gameDataSnapshotRegistration = docref.addSnapshotListener(this::onServerGameDataChanged)
    }

    fun updateGameData() {
        val docref = db.collection(COL_GAMES).document(runGameID)
        docref.update(GAME_DATA, runGameRaw.gameData)
    }

    fun updateGameData(value: String) {
        val docref = db.collection(COL_GAMES).document(runGameID)
        docref.update(GAME_DATA, FieldValue.arrayUnion(value))
    }

    fun deleteFromGameData(value: String) {
        val docref = db.collection(COL_GAMES).document(runGameID)
        docref.update(GAME_DATA, FieldValue.arrayRemove(value))
    }

    fun deleteLoadedGame() {
        if (this::gameDataSnapshotRegistration.isInitialized){
            gameDataSnapshotRegistration.remove()

        }

        runGameRaw.completionDate = System.currentTimeMillis()
        val docref = db.collection(COL_GAMES).document(runGameID)
        docref.update(GAME_COMPLETION_DATE, runGameRaw.completionDate)

        //db.collection(COL_GAMES).document(runGameID).delete()

        for (playerRef in runGameRaw.players) {
            db.collection(COL_USER)
                .document(playerRef.id).collection(USER_DATA)
                .document(USER_GAMES).update(USER_GAMES, FieldValue.arrayRemove(runGameID))
        }
    }



    fun getOwnUserID(): String {
        return Firebase.auth.currentUser!!.uid
    }
}