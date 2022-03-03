package com.example.group_d.ui.main.ingame

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.CompassLocation
import com.example.group_d.data.model.Game
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.random.Random

class CompassViewModel : GameViewModel() {
    val gson: Gson = Gson()
    private val locations: MutableList<CompassLocation> = ArrayList()

    private lateinit var random: Random
    private val _opponentName = MutableLiveData<String>()
    val opponentName: LiveData<String> = _opponentName

    private val _currentLocation = MutableLiveData<CompassLocation>()
    val currentLocation: LiveData<CompassLocation> = _currentLocation
    private val requestedLocationIndices: MutableList<Int> = ArrayList()

    private val _foundAllLocations = MutableLiveData<Boolean>()
    val foundAllLocations: LiveData<Boolean> = _foundAllLocations

    private val _gameIsOver = MutableLiveData<Boolean>()
    val gameIsOver: LiveData<Boolean> = _gameIsOver

    private var neededTimeSaved: Boolean = false

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
                    onGameDataChanged(gameData)
                    addGameDataChangedListener(docref)
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
                    neededTimeSaved = true
                    _foundAllLocations.value = true
                }
            }
        }
        if (readyPlayers == 2) {
            _gameIsOver.value = true
        }
    }

    fun loadLocations(json: String) {
        val locationArray = gson.fromJson(json, JsonObject::class.java).get("features").asJsonArray
        for (element in locationArray) {
            if (!element.isJsonObject) {
                continue
            }
            val jsonObj = element.asJsonObject
            val coordinates = jsonObj.get("geometry").asJsonObject.get("coordinates").asJsonArray.run {
                DoubleArray(2) { get(it).asDouble }
            }
            val jsonProps = jsonObj.get("properties").asJsonObject
            val name = jsonProps.get("Objekt").asString
            val addr = jsonProps.get("Adresse").asString
            locations.add(CompassLocation(name, coordinates, addr))
        }
        println()
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

    fun checkRightDirection(userPos: Location, orientation: Float): Boolean {
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
        var rightOrientation = acos(dotProd)
        if (diff[0] < 0) {
            rightOrientation = -rightOrientation
        }
        // tolerance of 10 degrees
        return abs(Math.toDegrees(rightOrientation) - orientation) <= 10
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
        if (neededTimeSaved) {
            return
        }
        val gameDataVal = "nT_${Firebase.auth.currentUser!!.email}=$neededTime"
        updateGameData(gameDataVal)
        neededTimeSaved = true
    }
}