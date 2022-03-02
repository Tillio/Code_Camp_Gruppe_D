package com.example.group_d.ui.main.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.CompassLocation
import com.example.group_d.data.model.Game
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlin.random.Random

class CompassViewModel : GameViewModel() {
    val gson: Gson = Gson()
    private val locations: MutableList<CompassLocation> = ArrayList()

    private lateinit var random: Random
    private val _opponentName = MutableLiveData<String>()
    val opponentName: LiveData<String> = _opponentName
    private val _currentLocation = MutableLiveData<CompassLocation>()
    val currentLocation: LiveData<CompassLocation> = _currentLocation

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getString(GAME_BEGINNER)?:"0"
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                playerRef.get().addOnSuccessListener { playerSnap ->
                    _opponentName.value = playerSnap.getString(USER_NAME)
                    val gameData = snap[GAME_DATA] as MutableList<String>
                    if (gameData.size > 0) {
                        random = Random(gameData[0].toLong())
                        for (i in 1 until gameData.size) {
                            var nextRandomVal = random.nextInt(locations.size)
                            while (gameData[i].toInt() != nextRandomVal) {
                                nextRandomVal = random.nextInt(locations.size)
                            }
                        }
                    } else {
                        val seed = Random.nextLong()
                        random = Random(seed)
                        gameData.add(seed.toString())
                    }
                    _currentLocation.value = locations[gameData[gameData.size - 1].toInt()]
                    runGame.value = Game(beginnerIndex, gameData, GAME_TYPE_TIC_TAC_TOE, playerRefs)
                    updateGameData()
                    addGameDataChangedListener(docref)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        // TODO("Not yet implemented")
    }

    fun loadLocations(json: String) {
        val locationArray = gson.fromJson(json, JsonObject::class.java).get("features").asJsonArray
        for (element in locationArray) {
            if (!element.isJsonObject) {
                continue
            }
            val jsonObj = element.asJsonObject
            val coordinates = jsonObj.get("geometry").asJsonObject.get("coordinates").asJsonArray.run {
                FloatArray(2) { get(it).asFloat }
            }
            val jsonProps = jsonObj.get("properties").asJsonObject
            val name = jsonProps.get("Objekt").asString
            val addr = jsonProps.get("Adresse").asString
            locations.add(CompassLocation(name, coordinates, addr))
        }
        println()
    }

    fun nextLocation() {
        var nextLocationIndex = random.nextInt(locations.size)
        while (nextLocationIndex.toString() in runGameRaw.gameData) {
            nextLocationIndex = random.nextInt(locations.size)
        }
        runGameRaw.gameData.add(nextLocationIndex.toString())
        updateGameData()
        _currentLocation.value = locations.get(nextLocationIndex)
    }
}