package com.minskrotterdam.airquality.math

import com.minskrotterdam.airquality.models.stations.Coordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun greatCircleDistance(from: Coordinates, to: Coordinates): Double {
    val earthRadius = 6371e3 // metres
    val φ1 = Math.toRadians(from.lat)
    val φ2 = Math.toRadians(to.lat)
    val Δφ = Math.toRadians(to.lat - from.lat)
    val Δλ = Math.toRadians(to.lng - from.lng)

    val a = sin(Δφ / 2) * sin(Δφ / 2) +
            cos(φ1) * cos(φ2) *
            sin(Δλ / 2) * sin(Δλ / 2)

    val c = 2 * atan2(Math.sqrt(a), sqrt(1 - a))

    return earthRadius * c
}