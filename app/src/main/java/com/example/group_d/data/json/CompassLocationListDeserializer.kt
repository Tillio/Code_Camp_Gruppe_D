package com.example.group_d.data.json

import com.example.group_d.data.model.CompassLocation
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class CompassLocationListDeserializer : JsonDeserializer<MutableList<CompassLocation>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MutableList<CompassLocation> {
        if (json == null) {
            return ArrayList()
        }
        val locationArray = json.asJsonObject.get("features").asJsonArray
        val locations = ArrayList<CompassLocation>(locationArray.size())
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
        return locations
    }
}