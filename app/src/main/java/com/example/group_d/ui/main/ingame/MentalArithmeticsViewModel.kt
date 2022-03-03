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
        var playerOneData: List<String> = emptyList()
        var playerTwoData: List<String> = emptyList()
        if(gameData.size > 1) {
            for (i in 1 until (gameData.size)) {
                val dataItem = gameData[i].split("=")
                if (dataItem[0] != Firebase.auth.currentUser!!.email && dataItem[1] == "finalTime") {
                    _opponentTime.value = dataItem[2]
                }
                if (dataItem[1] == "finalTime") {
                    if (playerOneData.isEmpty()) {
                        playerOneData = dataItem
                    } else if (playerOneData.isNotEmpty()) {
                        playerTwoData = dataItem
                    }
                }
            }
        }

        if(playerOneData.isNotEmpty() && playerTwoData.isNotEmpty()) {
            val playerOneName = playerOneData[0]
            val playerOneTime = playerOneData[2]
            val playerOneTimeData = playerOneTime.split(":")
            val playerOneSeconds = playerOneTimeData[0].toInt()*60 + playerOneTimeData[1].toInt()

            val playerTwoName = playerTwoData[0]
            val playerTwoTime = playerTwoData[2]
            val playerTwoTimeData = playerTwoTime.split(":")
            val playerTwoSeconds = playerTwoTimeData[0].toInt()*60 + playerTwoTimeData[1].toInt()

            if (playerTwoSeconds < playerOneSeconds) {
                _winner.value = playerTwoName
            } else {
                _winner.value = playerOneName
            }
        }
    }



}