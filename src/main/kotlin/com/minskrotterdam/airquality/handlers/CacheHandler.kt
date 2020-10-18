package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.cache.StationsCache
import com.minskrotterdam.airquality.models.stations.Coordinates
import com.minskrotterdam.airquality.models.stations.ExtData
import com.minskrotterdam.airquality.models.stations.Stations
import com.minskrotterdam.airquality.services.StationInfoService
import com.minskrotterdam.airquality.services.StationsService
import org.apache.commons.collections4.Trie
import org.apache.commons.collections4.trie.PatriciaTrie
import ru.gildor.coroutines.retrofit.await

class CacheHandler {

    suspend fun initStationsCache() {
        val firstPage = StationsService().getStations(1).await()
        cacheStationInfo(firstPage)
        (2..firstPage.pagination.last_page).map {
            val page = StationsService().getStations(it).await()
            cacheStationInfo(page)
        }
    }

    private suspend fun cacheStationInfo(firstPage: Stations) {
        firstPage.data.forEach { stationInfo ->
            val stationDetails = StationInfoService().getStationDetails(stationInfo.number).await()
            val rawCoordinates = stationDetails.data.geometry.coordinates
            val coordinates = Coordinates(rawCoordinates[0], rawCoordinates[1])
            val element = ExtData(stationInfo.number, stationInfo.location, stationDetails.data.municipality, coordinates, stationDetails.data.components)
            StationsCache.put(element.coordinates.lat.toString(), element)
        }
    }
}