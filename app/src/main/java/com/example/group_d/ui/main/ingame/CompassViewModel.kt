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

    // Quicker access to user's and opponent's start and end time without searching in game data
    private var _startTime: Long = 0L
    val startTime get() = _startTime

    private var _endTime: Long = 0L
    val endTime get() = _endTime

    private var opStartTime: Long = 0L
    private var opEndTime: Long = 0L

    // the UserID of the other player
    var otherID: String = ""
    var otherName: String = ""

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getString(GAME_BEGINNER)?:"0"
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                // get the ID of the other player
                otherID = playerRef.id
                // get the name of the other player
                playerRef.get().addOnSuccessListener { document ->
                   otherName = document["name"].toString() }
                playerRef.get().addOnSuccessListener { playerSnap ->
                    _opponentName.value = playerSnap.getString(USER_DISPLAY_NAME)
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
                        if (str.startsWith("sT_")) {
                            // Extract start times
                            val time = str.split("=")[1].toLong()
                            if (str.startsWith("sT_${Firebase.auth.currentUser!!.email}=")) {
                                // Save user start time
                                _startTime = time
                            } else {
                                // Save opponent start time
                                opStartTime = time
                            }
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
            if (str.startsWith("sT_")
                && !str.startsWith("sT_${Firebase.auth.currentUser!!.email}=")) {
                // Opponent started the game, save start time
                opStartTime = str.split("=")[1].toLong()
            }
            else if (str.startsWith("eT_")) {
                readyPlayers += 1
                // Extract time
                val time = str.split("=")[1].toLong()
                if (str.startsWith("eT_${Firebase.auth.currentUser!!.email}=")) {
                    // User has finished
                    _endTime = time
                    if (_endTime < 0) {
                        // user gave up
                        readyPlayers = 2
                        _foundAllLocations.value = false
                    }
                    _foundAllLocations.value = true
                } else {
                    // Opponent has finished
                    opEndTime = time
                    if (opEndTime < 0) {
                        // opponent gave up
                        readyPlayers = 2
                    }
                }
            }
        }
        if (readyPlayers >= 2) {
            val neededTime = _endTime - _startTime
            val opNeededTime = opEndTime - opStartTime
            val timeDiff = opNeededTime - neededTime
            _ending.value = when {
                timeDiff == 0L -> GameEnding.DRAW
                // user gave up
                _endTime < 0L -> GameEnding.LOSE
                // opponent gave up
                opEndTime < 0L -> GameEnding.WIN
                timeDiff > 0L -> GameEnding.WIN
                else -> GameEnding.LOSE
            }
        }
    }

    // Called when the user found a location
    fun nextLocation() {
        if (requestedLocationIndices.isNotEmpty()) {
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
        val locationEnuNorm = sqrt(locationEnu.sumOf { it * it })
        val magneticNorthEnuNorm = sqrt(magneticNorthEnu.sumOf { it * it })
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

    fun saveStartTime(startTime: Long) {
        val gameDataVal = "sT_${Firebase.auth.currentUser!!.email}=$startTime"
        updateGameData(gameDataVal)
        this._startTime = startTime
    }

    fun saveEndTime(endTime: Long) {
        val gameDataVal = "eT_${Firebase.auth.currentUser!!.email}=$endTime"
        updateGameData(gameDataVal)
        this._endTime = endTime
    }

    fun giveUp() {
        saveEndTime(-1)
    }
}