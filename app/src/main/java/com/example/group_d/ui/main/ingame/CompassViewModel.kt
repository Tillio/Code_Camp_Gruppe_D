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
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.random.Random

class CompassViewModel : GameViewModel() {
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

    private var _neededTime: Long = 0
    val neededTime get() = _neededTime

    private var opNeededTime: Long = 0

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getString(GAME_BEGINNER)?:"0"
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                playerRef.get().addOnSuccessListener { playerSnap ->
                    _opponentName.value = playerSnap.getString(USER_NAME)
                    val gameData = snap[GAME_DATA] as MutableList<String>
                    // Initialize random generator with the saved seed
                    random = Random(gameData[0].toLong())
                    /*
                        Generate the indices for the locations which will be asked:
                        since both players have the same seed they will get the same locations
                     */
                    for (i in 1..10) {
                        var nextLocationIndex = random.nextInt(locations.size)
                        // Don't ask twice for the same location in one game
                        while (nextLocationIndex in requestedLocationIndices) {
                            nextLocationIndex = random.nextInt(locations.size)
                        }
                        requestedLocationIndices.add(nextLocationIndex)
                    }
                    for (str in gameData) {
                        if (str.startsWith("fL_${Firebase.auth.currentUser!!.email}=")) {
                            /*
                                Get the number of locations the user already found and remove the
                                corresponding indices from the list
                             */
                            val foundLocations = str.split("=")[1].toInt()
                            for (i in 1..foundLocations) {
                                requestedLocationIndices.removeFirst()
                            }
                            break
                        }
                    }
                    // Show the next location
                    _currentLocation.value = locations[requestedLocationIndices.removeFirst()]
                    // Notify the fragment that the game is loaded
                    runGame.value = Game(beginnerIndex, gameData, GAME_TYPE_TIC_TAC_TOE, playerRefs)
                    addGameDataChangedListener(docref)
                    // Fire onGameDataChanged in case a player is ready
                    onGameDataChanged(gameData)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        // Count ready players
        var readyPlayers = 0
        for (str in gameData) {
            if (str.startsWith("nT_")) {
                readyPlayers += 1
                if (str.startsWith("nT_${Firebase.auth.currentUser!!.email}=")) {
                    // User has finished, extract time
                    _neededTime = str.split("=")[1].toLong()
                    if (_neededTime < 0) {
                        // user gave up
                        readyPlayers = 2
                        break
                    }
                    _foundAllLocations.value = true
                } else {
                    // Opponent has finished, extract time
                    opNeededTime = str.split("=")[1].toLong()
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
                timeDiff == 0L -> GameEnding.DRAW
                // user gave up
                _neededTime < 0L -> GameEnding.LOSE
                // opponent gave up
                opNeededTime < 0L -> GameEnding.WIN
                timeDiff > 0L -> GameEnding.WIN
                else -> GameEnding.LOSE
            }
        }
    }

    // Called when the user found a location
    fun nextLocation() {
        if (!requestedLocationIndices.isEmpty()) {
            // use postValue instead of setValue because this method is called from the timer thread
            _currentLocation.postValue(locations[requestedLocationIndices.removeFirst()])
        } else {
            _foundAllLocations.postValue(true)
        }
        // Delete old number of found locations
        val oldGameDataVal =
            "fL_${Firebase.auth.currentUser!!.email}=${10 - requestedLocationIndices.size - 2}"
        deleteFromGameData(oldGameDataVal)
        // Save new number of found locations
        val gameDataVal = "fL_${Firebase.auth.currentUser!!.email}=${10 - requestedLocationIndices.size - 1}"
        updateGameData(gameDataVal)
    }

    private fun getRightDirection(userPos: Location): Double {
        // Set altitudes to 0 because the geoportal doesn't provide them
        val userPosEcef =
            CompassLocation.geodeticToEcef(userPos.latitude, userPos.longitude, 0.0)
        val currentLocationEcef = _currentLocation.value!!.run {
            CompassLocation.geodeticToEcef(latitude, longitude, 0.0)
        }
        val locationEnu = CompassLocation.ecefToEnu(userPosEcef, currentLocationEcef)
        val magneticNorthEnu = CompassLocation.ecefToEnu(userPosEcef, CompassLocation.magneticNorthEcef)
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

    fun getDirectionError(lastUserPosition: Location, lastOrientation: Float): Double {
        val rightOrientation = getRightDirection(lastUserPosition)
        var error = lastOrientation - rightOrientation
        if (error > 180.0) {
            /*
                if e.g. lastOrientation == 179 and rightOrientation == -179,
                error would be 358 although the real difference is only 2 degrees
                -> subtract 360 to correct this
             */
            error -= 360
        } else if (error < -180.0) {
            // similarly add 360 if error < -180
            error += 360
        }
        return error
    }

    private fun timeCharSequenceToLong(timeCS: CharSequence): Long {
        return timeCS.split(":").run {
            var result = 0L
            forEach {
                result *= 60
                result += it.toLong()
            }
            result
        }
    }

    fun loadStartTime(): Long {
        for (str in runGameRaw.gameData) {
            if (str.startsWith("sT_${Firebase.auth.currentUser!!.email}=")) {
                return str.split("=")[1].toLong()
            }
        }
        // Start time not saved yet -> return 0
        return 0L
    }

    fun saveStartTime(startTime: Long) {
        val gameDataVal = "sT_${Firebase.auth.currentUser!!.email}=$startTime"
        updateGameData(gameDataVal)
    }

    fun saveNeededTime(neededTime: Long) {
        val gameDataVal = "nT_${Firebase.auth.currentUser!!.email}=$neededTime"
        updateGameData(gameDataVal)
        this._neededTime = neededTime
    }

    fun saveNeededTime(neededTimeCS: CharSequence) {
        val neededTime = timeCharSequenceToLong(neededTimeCS)
        saveNeededTime(neededTime)
    }

    fun giveUp() {
        saveNeededTime(-1)
    }
}