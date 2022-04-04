package com.example.group_d.data.json

import com.example.group_d.*
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
        if (json == null || !json.isJsonObject) {
            // Return empty list if we cannot deserialize the json element
            return ArrayList()
        }
        val locationArray = json.asJsonObject.get(JSON_FEATURES).asJsonArray
        val locations = ArrayList<CompassLocation>(locationArray.size())
        for (element in locationArray) {
            if (!element.isJsonObject) {
                // Ignore the element
                continue
            }
            // Deserialize the location and add it to locations
            val jsonObj = element.asJsonObject
            val coordinates = jsonObj
                .get(JSON_GEOMETRY).asJsonObject
                .get(JSON_GEOMETRY_COORDINATES).asJsonArray
            val latitude = coordinates[1].asDouble
            val longitude = coordinates[0].asDouble
            val jsonProps = jsonObj.get(JSON_PROPS).asJsonObject
            val name = jsonProps.get(JSON_PROPS_NAME).asString
            val addr = jsonProps.get(JSON_PROPS_ADDRESS).asString
            locations.add(CompassLocation(name, latitude, longitude, addr))
        }
        return locations
    }
}