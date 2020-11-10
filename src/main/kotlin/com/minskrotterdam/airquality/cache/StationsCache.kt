package com.minskrotterdam.airquality.cache

import com.minskrotterdam.airquality.math.greatCircleDistance
import com.minskrotterdam.airquality.models.stations.Coordinates
import com.minskrotterdam.airquality.models.stations.ExtData
import org.apache.commons.collections4.Trie
import org.apache.commons.collections4.trie.PatriciaTrie
import java.math.MathContext
import java.math.RoundingMode

object StationsCache {
    private var stationsByLtd: Trie<String, ExtData> = PatriciaTrie()

    fun put(data: ExtData) {
        stationsByLtd[data.coordinates.lat.toString()] = data
    }

    fun getStation(lat: String, lng: String): ExtData {
        var endInclusive = 4
        val latRound = "%.4f".format(lat.toBigDecimal().round(MathContext(6, RoundingMode.FLOOR)))
        val latRoundTruncated = lat.substring(0..lat.indexOf(".") - 1)
        val latRoundHalfEven = "%.0f".format(lat.toBigDecimal().round(MathContext(0, RoundingMode.HALF_EVEN)))
        var extendPrefixMap = false
        if (!latRoundTruncated.equals(latRoundHalfEven)) {
            extendPrefixMap = true
        } else {
            endInclusive--
        }
        var result = stationsByLtd.prefixMap(latRound)
        while (result.isEmpty() && endInclusive > 0) {
            result = stationsByLtd.prefixMap(lat.substring(0..--endInclusive))
        }
        val coordinates = Coordinates(lng.toDouble(), lat.toDouble())
        val candidates: MutableList<ExtData> = mutableListOf<ExtData>()
        candidates.add(result.values.reduce { acc, data -> withClosestGreatCircleDistance(acc, data, coordinates) })
        if (extendPrefixMap) {
            val prefixMap = stationsByLtd.prefixMap(latRoundHalfEven)
            if (!prefixMap.isEmpty()) {
                candidates.add(prefixMap.values.reduce { acc, data -> withClosestGreatCircleDistance(acc, data, coordinates) })
            }
        }
        return candidates.reduce { acc, data -> withClosestGreatCircleDistance(acc, data, coordinates) }
    }

    private fun withClosestGreatCircleDistance(o1: ExtData, o2: ExtData, coordinates: Coordinates): ExtData {
        val gcd1 = greatCircleDistance(o1.coordinates, coordinates)
        val gcd2 = greatCircleDistance(o2.coordinates, coordinates)
        return if (gcd1 < gcd2) o1 else o2
    }

}