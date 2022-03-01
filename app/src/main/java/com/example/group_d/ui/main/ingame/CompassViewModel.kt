package com.example.group_d.ui.main.ingame

import com.example.group_d.data.model.CompassLocation
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.JsonObject

class CompassViewModel : GameViewModel() {
    val gson: Gson = Gson()
    private val _locations: MutableList<CompassLocation> = ArrayList()

    // TODO: Implement the ViewModel
    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        TODO("Not yet implemented")
    }

    override fun onGameDataChanged(gameData: List<String>) {
        TODO("Not yet implemented")
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
            val name = jsonObj.get("properties").asJsonObject.get("Objekt").asString
            _locations.add(CompassLocation(name, coordinates))
        }
        println()
    }
}