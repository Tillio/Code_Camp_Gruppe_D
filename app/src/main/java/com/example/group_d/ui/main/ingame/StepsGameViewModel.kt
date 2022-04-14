package com.example.group_d.ui.main.ingame

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.GAME_PLAYERS
import com.example.group_d.GAME_TYPE_STEPS_GAME
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StepsGameViewModel : GameViewModel(), SensorEventListener {

    private val _stepsWinner = MutableLiveData<String>()
    val stepsWinner: LiveData<String> = _stepsWinner

    private val _actualSteps = MutableLiveData<Int>(0)
    var actualSteps: LiveData<Int> = _actualSteps

    var stepsBase: Int = 0

    private val db = Firebase.firestore

    private val _opponentSteps = MutableLiveData<String>()
    val opponentSteps: LiveData<String> = _opponentSteps

    lateinit var gameID: String

    lateinit var sensorManager: SensorManager

    // the UserID of the other player
    var otherID: String = ""
    var otherName: String = ""

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                // get the ID of the other player
                otherID = playerRef.id
                // get the name of the other player
                playerRef.get().addOnSuccessListener { document ->
                    otherName = document["name"].toString() }
                playerRef.get().addOnSuccessListener { playerSnap ->
                    val gameData = snap[GAME_DATA] as MutableList<String>
                    val gameDataString: MutableList<String> = gameData
                    runGame.value = Game("0", gameDataString, GAME_TYPE_STEPS_GAME, playerRefs)
                    addGameDataChangedListener(docref)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        var playerOneData: List<String> = emptyList()
        var playerTwoData: List<String> = emptyList()
        if(gameData.isNotEmpty()) {
            for (i in 0 until (gameData.size)) {
                val dataItem = gameData[i].split("=")
                if (dataItem[0] != Firebase.auth.currentUser!!.email && dataItem[1] == "finalStepsAmount") {
                    _opponentSteps.value = dataItem[2]
                }
                if (dataItem[1] == "finalStepsAmount") {
                    if (playerOneData.isEmpty()) {
                        playerOneData = dataItem
                    } else if (playerOneData.isNotEmpty()) {
                        playerTwoData = dataItem
                    }
                }
            }
        }

        //winner is calculated
        if(playerOneData.isNotEmpty() && playerTwoData.isNotEmpty()) {
            val playerOneName = playerOneData[0]
            val playerOneSteps = playerOneData[2].toInt()

            val playerTwoName = playerTwoData[0]
            val playerTwoSteps = playerTwoData[2].toInt()

            if (playerTwoSteps > playerOneSteps) {
                _stepsWinner.value = playerTwoName
            } else {
                _stepsWinner.value = playerOneName
            }
        }
    }

    //step sensor is started
    fun startStepCounter() {
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCounterSensor.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    //step sensor is stopped
    fun stopStepCounter() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent ?: return
        if (stepsBase == 0) {
            stepsBase = sensorEvent.values.firstOrNull()!!.toInt()
        }

        //refreshes current steps in the database
        sensorEvent.values.firstOrNull()?.let {
            db.collection(COL_GAMES).document(gameID).update(
                GAME_DATA, FieldValue.arrayRemove(
                    Firebase.auth.currentUser!!.email + "=" + "currentSteps" + "=" + _actualSteps.value
                )
            )

            _actualSteps.value = (it.toInt() - stepsBase)

            db.collection(COL_GAMES).document(gameID).update(
                GAME_DATA, FieldValue.arrayUnion(
                    Firebase.auth.currentUser!!.email + "=" + "currentSteps" + "=" + _actualSteps.value))
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun showEndstate(gameID: String) {

    }

}