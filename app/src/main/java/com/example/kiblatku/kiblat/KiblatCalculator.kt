package com.example.kiblatku.kiblat

import kotlin.math.*

object KiblatCalculator {

    fun hitungArahKiblat(
        userLat: Double,
        userLon: Double
    ): Double {

        val lat1 = Math.toRadians(userLat)
        val lon1 = Math.toRadians(userLon)
        val lat2 = Math.toRadians(KiblatConstants.MEKAH_LAT)
        val lon2 = Math.toRadians(KiblatConstants.MEKAH_LON)

        val dLon = lon2 - lon1

        val y = sin(dLon)
        val x = cos(lat1) * tan(lat2) - sin(lat1) * cos(dLon)

        var arah = Math.toDegrees(atan2(y, x))
        arah = (arah + 360) % 360

        return arah
    }
}
