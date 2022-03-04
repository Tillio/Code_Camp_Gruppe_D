package com.example.group_d.ui.main.recentGames.statistiks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.ui.main.recentGames.statistiks.StatisticsRecyclerItem.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StatisticsViewModel : ViewModel() {


    private val db = Firebase.firestore

    val _pastGamesData = MutableLiveData<ArrayList<StatisticsRecyclerItem>>()
    val pastGamesData : LiveData<ArrayList<StatisticsRecyclerItem>> = _pastGamesData

    val tttStats = TicTacToe(0, 0)

    val compass = Compass(0, 0, "00:00")

    val mentalArithmetic = Mental_Arithmetic(0, 0, "00:00")

    val stepsChallenge = Steps_Challenge(0, 0, hashMapOf(), 0)

    init {
        getPastGameData()
    }

    fun getPastGameData() {

        _pastGamesData.value= arrayListOf(tttStats, compass, mentalArithmetic, stepsChallenge)

        db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).get()
            .addOnSuccessListener { it ->
                val doc = it.reference
                db.collection(COL_GAMES).whereArrayContains(GAME_PLAYERS, doc)
                    .whereNotEqualTo(
                        GAME_COMPLETION_DATE, 0L
                    ).get().addOnCompleteListener() { qs ->
                        for (gameDoc in qs.result.documents) {
                            processPastGameData(gameDoc)
                            val value = _pastGamesData.value
                            _pastGamesData.postValue(value!!)
                        }
                    }
            }
    }


    private fun processPastGameData(gameDoc: DocumentSnapshot) {
        when (gameDoc[GAME_TYPE]) {
            GAME_TYPE_TIC_TAC_TOE -> handleTTTGame(gameDoc)
            GAME_TYPE_MENTAL_ARITHMETICS -> handleArithmeticsGame(gameDoc)
            GAME_TYPE_STEPS_GAME -> handleStepsGame(gameDoc)

        }
    }

    private fun handleStepsGame(gameDoc: DocumentSnapshot) {
        stepsChallenge.totalGames += 1

    }

    private fun handleArithmeticsGame(gameDoc: DocumentSnapshot) {
        mentalArithmetic.totalGames += 1
        handleArithmeticsGameData(gameDoc[GAME_DATA] as ArrayList<String>)

    }

    private fun handleTTTGame(gameDoc: DocumentSnapshot) {
        tttStats.totalGames += 1
        tttStats.wins += 1

    }

    private fun handleArithmeticsGameData(gameData: ArrayList<String>) {
        for (i in 1 until (gameData.size)) {
            val dataItem = gameData[i].split("=")
            if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "finalTime")) {
                mentalArithmetic.best_time_in_s =
                    compareFinishTime(mentalArithmetic.best_time_in_s, dataItem[2] as String)
            }
        }

    }


    private fun compareFinishTime(t1: String, t2: String): String {
        if(t1 == "00:00"){
            return t2
        }
        val split1 = t1.split(":")
        val split2 = t2.split(":")

        for (i in 0 until 1) {
            val i1 = split1[i].toInt()
            val i2 = split2[i].toInt()

            if (i1 < i2) {
                return t1
            } else if (i2 < i1) {
                return t2

            }
        }
        return t1

    }
}