package com.minskrotterdam.airquality.extensions

import com.minskrotterdam.airquality.math.greatCircleDistance
import com.minskrotterdam.airquality.models.stations.Coordinates
import com.minskrotterdam.airquality.models.stations.ExtData
import org.apache.commons.collections4.Trie

private fun withClosestGreatCircleDistance(o1: ExtData, o2: ExtData, coordinates: Coordinates): ExtData {
    val gcd1 = greatCircleDistance(o1.coordinates, coordinates)
    val gcd2 = greatCircleDistance(o2.coordinates, coordinates)
    return if (gcd1 < gcd2) o1 else o2
}

fun searchByLatLng(stations: Trie<String, ExtData>, lat: String, lng: String): ExtData {
    var endInclusive = 4
    val latRound = "%.4f".format(lat.toDouble())
    var result = stations.prefixMap(latRound)
    // println("${lat}.substr(${0..endInclusive}) /${lat.substring(0..endInclusive)}/ -> ${result.size} results")
    while (result.isEmpty() && endInclusive > 0) {
        result = stations.prefixMap(lat.substring(0..--endInclusive))
        // println("${lat}.substr(${0..endInclusive}) /${lat.substring(0..endInclusive)}/ -> ${result.size} results")
    }
    val coordinates = Coordinates(lng.toDouble(), lat.toDouble())
    return result.values.reduce { acc, data -> withClosestGreatCircleDistance(acc, data, coordinates) }
}


