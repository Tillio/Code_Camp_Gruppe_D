package com.example.group_d.ui.main.recentGames.statistiks

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.group_d.*
import com.example.group_d.ui.main.recentGames.statistiks.StatisticsRecyclerItem.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {


    private val db = Firebase.firestore

    private val _pastGamesData = MutableLiveData<ArrayList<StatisticsRecyclerItem>>()
    var pastGamesData: LiveData<ArrayList<StatisticsRecyclerItem>> = _pastGamesData

    private var tttStats = TicTacToe()

    private var compass = Compass("00:00")

    private var mentalArithmetic = Mental_Arithmetic("00:00")

    private var stepsChallenge = Steps_Challenge(hashMapOf(), 0)

    init {
        //updateData()
    }

    private fun getPastGameData() {
        viewModelScope.launch {
            _pastGamesData.postValue(
                arrayListOf(
                    tttStats,
                    compass,
                    mentalArithmetic,
                    stepsChallenge
                )
            )
            db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).get()
                .addOnSuccessListener { it ->
                    val doc = it.reference
                    db.collection(COL_GAMES).whereArrayContains(GAME_PLAYERS, doc)
                        .whereNotEqualTo(
                            GAME_COMPLETION_DATE, 0L
                        ).get().addOnCompleteListener() { qs ->
                            for (gameDoc in qs.result.documents) {
                                processPastGameData(gameDoc)
                                val newValues =
                                    arrayListOf(tttStats, compass, mentalArithmetic, stepsChallenge)

                                _pastGamesData.postValue(newValues)
                            }
                        }
                }
        }

    }


    private fun processPastGameData(gameDoc: DocumentSnapshot) {
        val completionDate = gameDoc[GAME_COMPLETION_DATE]
        if (completionDate != null) {
            when (gameDoc[GAME_TYPE]) {
                GAME_TYPE_TIC_TAC_TOE -> handleTTTGame(gameDoc)
                GAME_TYPE_MENTAL_ARITHMETICS -> handleArithmeticsGame(gameDoc)
                GAME_TYPE_STEPS_GAME -> handleStepsGame(gameDoc)
                GAME_TYPE_COMPASS -> handleCompassGame(gameDoc)

            }
        }
    }


    private fun handleCompassGame(gameDoc: DocumentSnapshot) {
        countGamesAndWins(compass, gameDoc)
    }

    private fun handleStepsGame(gameDoc: DocumentSnapshot) {
        countGamesAndWins(stepsChallenge, gameDoc)
    }

    private fun handleArithmeticsGame(gameDoc: DocumentSnapshot) {
        countGamesAndWins(mentalArithmetic, gameDoc)
    }

    private fun handleTTTGame(gameDoc: DocumentSnapshot) {
        countGamesAndWins(tttStats, gameDoc)
    }

    private fun countGamesAndWins(gameTypeItem: StatisticsRecyclerItem, gameDoc: DocumentSnapshot) {
        gameTypeItem.totalGames += 1
        val winnerId = gameDoc[GAME_WINNER]
        if (winnerId != null) {
            if (winnerId == FirebaseAuth.getInstance().uid) {
                gameTypeItem.wins += 1
            }
        }
    }

    fun updateData() {
        resetData()
        getPastGameData()
    }

    private fun resetData() {
        tttStats.wins = 0
        tttStats.totalGames = 0

        compass.wins = 0
        compass.totalGames = 0

        mentalArithmetic.wins = 0
        mentalArithmetic.totalGames = 0

        stepsChallenge.wins = 0
        stepsChallenge.totalGames = 0

    }

    fun pieData(): PieData {
        val entries: ArrayList<PieEntry> = ArrayList()
        var label = ""

        val wins = tttStats.wins + compass.wins + stepsChallenge.wins + mentalArithmetic.wins
        val total =
            tttStats.totalGames + compass.totalGames + stepsChallenge.totalGames + mentalArithmetic.totalGames

        var data: HashMap<String, Int> = HashMap()
        data.put("Wins", wins)
        data.put("Losses", total - wins)

        var colors: ArrayList<Int> = ArrayList()
        colors.add(Color.parseColor("#2f63f5"))
        colors.add(Color.parseColor("#e14646"))

        for (type in data.keys) {
            data.get(type)?.let { PieEntry(it.toFloat(), type) }?.let { entries.add(it) }
        }

        var pieDataSet = PieDataSet(entries, label)

        pieDataSet.valueTextSize = 12f
        pieDataSet.colors = colors
        var pieData = PieData(pieDataSet)
        pieData.setDrawValues(true)


        return pieData

    }
}
