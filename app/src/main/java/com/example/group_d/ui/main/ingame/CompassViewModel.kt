package com.example.group_d.ui.main.ingame

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.CompassLocation
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.random.Random

class CompassViewModel : GameViewModel() {
    val gson: Gson = Gson()
    lateinit var locations: MutableList<CompassLocation>

    private lateinit var random: Random
    private val _opponentName = MutableLiveData<String>()
    val opponentName: LiveData<String> = _opponentName

    private val _currentLocation = MutableLiveData<CompassLocation>()
    val currentLocation: LiveData<CompassLocation> = _currentLocation
    private val requestedLocationIndices: MutableList<Int> = ArrayList()

    private val _foundAllLocations = MutableLiveData<Boolean>()
    val foundAllLocations: LiveData<Boolean> = _foundAllLocations

    private val _ending = MutableLiveData<GameEnding>()
    val ending: LiveData<GameEnding> = _ending

    private var _neededTime: Int = 0
    val neededTime get() = _neededTime

    private var opNeededTime: Int = 0

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getString(GAME_BEGINNER)?:"0"
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                playerRef.get().addOnSuccessListener { playerSnap ->
                    _opponentName.value = playerSnap.getString(USER_NAME)
                    val gameData = snap[GAME_DATA] as MutableList<String>
                    random = Random(gameData[0].toLong())
                    for (i in 1..10) {
                        var nextLocationIndex = random.nextInt(locations.size)
                        while (nextLocationIndex in requestedLocationIndices) {
                            nextLocationIndex = random.nextInt(locations.size)
                        }
                        requestedLocationIndices.add(nextLocationIndex)
                    }
                    for (str in gameData) {
                        if (str.startsWith("fL_${Firebase.auth.currentUser!!.email}=")) {
                            val foundLocations = str.split("=")[1].toInt()
                            for (i in 1..foundLocations) {
                                requestedLocationIndices.removeFirst()
                            }
                            break
                        }
                    }
                    _currentLocation.value = locations[requestedLocationIndices.removeFirst()]
                    runGame.value = Game(beginnerIndex, gameData, GAME_TYPE_TIC_TAC_TOE, playerRefs)
                    addGameDataChangedListener(docref)
                    onGameDataChanged(gameData)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        var readyPlayers = 0
        for (str in gameData) {
            if (str.startsWith("nT_")) {
                readyPlayers += 1
                if (str.startsWith("nT_${Firebase.auth.currentUser!!.email}=")) {
                    _neededTime = str.split("=")[1].toInt()
                    if (_neededTime < 0) {
                        // user gave up
                        readyPlayers = 2
                        break
                    }
                    _foundAllLocations.value = true
                } else {
                    opNeededTime = str.split("=")[1].toInt()
                    if (opNeededTime < 0) {
                        // opponent gave up
                        readyPlayers = 2
                        break
                    }
                }
            }
        }
        if (readyPlayers == 2) {
            val timeDiff = opNeededTime - _neededTime
            _ending.value = when {
                timeDiff == 0 -> GameEnding.DRAW
                // user gave up
                _neededTime < 0 -> GameEnding.LOSE
                // opponent gave up
                opNeededTime < 0 -> GameEnding.WIN
                timeDiff > 0 -> GameEnding.WIN
                else -> GameEnding.LOSE
            }
        }
    }

    fun nextLocation() {
        if (!requestedLocationIndices.isEmpty()) {
            // use postValue instead of setValue because this method is called from another thread
            _currentLocation.postValue(locations[requestedLocationIndices.removeFirst()])
        } else {
            _foundAllLocations.postValue(true)
        }
        val oldGameDataVal =
            "fL_${Firebase.auth.currentUser!!.email}=${10 - requestedLocationIndices.size - 2}"
        deleteFromGameData(oldGameDataVal)
        val gameDataVal = "fL_${Firebase.auth.currentUser!!.email}=${10 - requestedLocationIndices.size - 1}"
        updateGameData(gameDataVal)
    }

    fun getRightDirection(userPos: Location): Double {
        // set altitudes to 0 because the geoportal doesn't provide them
        val userPosEcef =
            CompassLocation.geodeticToEcef(userPos.latitude, userPos.longitude, 0.0)
        val currentLocationEcef = _currentLocation.value!!.run {
            CompassLocation.geodeticToEcef(coordinates[1], coordinates[0], 0.0)
        }
        val locationEnu = CompassLocation.EcefToEnu(userPosEcef, currentLocationEcef)
        val magneticNorthEnu = CompassLocation.EcefToEnu(userPosEcef, CompassLocation.magneticNorthEcef)
        val locationEnuNorm = sqrt(locationEnu.map { it * it }.sum())
        val magneticNorthEnuNorm = sqrt(magneticNorthEnu.map { it * it }.sum())
        val locationDir = locationEnu.map { it / locationEnuNorm }
        val magneticNorthDir = magneticNorthEnu.map { it / magneticNorthEnuNorm }
        val diff = locationDir.mapIndexed { i, d -> d - magneticNorthDir[i] }
        val dotProd = locationDir.mapIndexed { i, d -> d * magneticNorthDir[i] }.sum()
        var rightOrientation = Math.toDegrees(acos(dotProd))
        if (diff[0] < 0) {
            rightOrientation = -rightOrientation
        }
        return rightOrientation
    }

    fun loadTimerBase(): Long {
        for (str in runGameRaw.gameData) {
            if (str.startsWith("tB_${Firebase.auth.currentUser!!.email}=")) {
                return str.split("=")[1].toLong()
            }
        }
        return 0L
    }

    fun saveTimerBase(timerBase: Long) {
        val gameDataVal = "tB_${Firebase.auth.currentUser!!.email}=$timerBase"
        updateGameData(gameDataVal)
    }

    fun saveNeededTime(neededTime: Int) {
        val gameDataVal = "nT_${Firebase.auth.currentUser!!.email}=$neededTime"
        updateGameData(gameDataVal)
        this._neededTime = neededTime
    }

    fun giveUp() {
        saveNeededTime(-1)
    }
}