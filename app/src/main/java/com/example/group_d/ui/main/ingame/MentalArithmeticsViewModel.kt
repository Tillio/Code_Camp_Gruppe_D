package com.example.group_d.ui.main.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase

class MentalArithmeticsViewModel : GameViewModel() {

    private val _winner = MutableLiveData<String>()
    val winner: LiveData<String> = _winner

    private val _opponentTime = MutableLiveData<String>()
    val opponentTime: LiveData<String> = _opponentTime

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                playerRef.get().addOnSuccessListener { playerSnap ->
                    val gameData = snap[GAME_DATA] as MutableList<String>
                    val gameDataString: MutableList<String> = gameData
                    runGame.value = Game("0", gameDataString, GAME_TYPE_MENTAL_ARITHMETICS, playerRefs)
                    addGameDataChangedListener(docref)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        if(gameData.size > 1) {
            for (i in 1..(gameData.size - 1)) {
                val dataItem = gameData[i].split("=")
                if (dataItem.get(0) != Firebase.auth.currentUser!!.email) {
                    _opponentTime.value = dataItem.get(1)
                }
            }
        }

        if(gameData.size == 3) {
            val playerOneData = gameData.get(1).split("=")
            val playerOneName = playerOneData.get(0)
            val playerOneTime = playerOneData.get(1)
            val playerOneTimeData = playerOneTime.split(":")
            val playerOneSeconds = playerOneTimeData.get(0).toInt()*60 + playerOneTimeData.get(1).toInt()

            val playerTwoData = gameData.get(2).split("=")
            val playerTwoName = playerTwoData.get(0)
            val playerTwoTime = playerTwoData.get(1)
            val playerTwoTimeData = playerTwoTime.split(":")
            val playerTwoSeconds = playerTwoTimeData.get(0).toInt()*60 + playerTwoTimeData.get(1).toInt()

            if (playerTwoSeconds < playerOneSeconds) {
                _winner.value = playerTwoName
            } else {
                _winner.value = playerOneName
            }
        }
    }

}