package com.example.group_d.data.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CompassLocation(val name: String, val latitude: Double, val longitude: Double, val addr: String) {
    companion object {
        private const val E_RADIUS = 6_378_137.0
        private const val P_RADIUS = 6_356_752.3
        private const val ECC_2 = 1 - (P_RADIUS * P_RADIUS) / (E_RADIUS * E_RADIUS)
        private const val MAGNETIC_NORTH_LAT = 86.494
        private const val MAGNETIC_NORTH_LONG = 162.867
        private const val MAGNETIC_NORTH_ALT = 0.0
        val magneticNorthEcef: EcefLocation by lazy {
            geodeticToEcef(MAGNETIC_NORTH_LAT, MAGNETIC_NORTH_LONG, MAGNETIC_NORTH_ALT)
        }

        fun geodeticToEcef(latitude: Double, longitude: Double, altitude: Double): EcefLocation {
            val latRad = Math.toRadians(latitude)
            val longRad = Math.toRadians(longitude)
            val sinLat = sin(latRad)
            val cosLat = cos(latRad)
            val sinLong = sin(longRad)
            val cosLong = cos(longRad)
            val n = E_RADIUS / sqrt(1 - ECC_2 * sinLat * sinLat)

            val x = (n + altitude) * cosLat * cosLong
            val y = (n + altitude) * cosLat * sinLong
            val z = ((P_RADIUS * P_RADIUS) / (E_RADIUS * E_RADIUS) * n + altitude) * sinLat
            return EcefLocation(x, y, z, latRad, longRad)
        }

        fun ecefToEnu(refPoint: EcefLocation, destPoint: EcefLocation): DoubleArray {
            val sinLatRef = sin(refPoint.latitudeRad)
            val cosLatRef = cos(refPoint.latitudeRad)
            val sinLongRef = sin(refPoint.longitudeRad)
            val cosLongRef = cos(refPoint.longitudeRad)
            val diffX = destPoint.x - refPoint.x
            val diffY = destPoint.y - refPoint.y
            val diffZ = destPoint.z - refPoint.z

            val x = -sinLongRef * diffX + cosLongRef * diffY
            val y =
                -sinLatRef * cosLongRef * diffX - sinLatRef * sinLongRef * diffY + cosLatRef * diffZ
            val z =
                cosLatRef * cosLongRef * diffX + cosLatRef * sinLongRef * diffY + sinLatRef * diffZ
            return doubleArrayOf(x, y, z)
        }
    }

    class EcefLocation(
        val x: Double, val y: Double, val z: Double,
        val latitudeRad: Double, val longitudeRad: Double
    )
}