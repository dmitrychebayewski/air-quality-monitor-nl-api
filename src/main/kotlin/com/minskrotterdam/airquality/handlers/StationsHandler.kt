package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.getSafeLaunchRanges
import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.extensions.searchByLatLng
import com.minskrotterdam.airquality.models.stations.Coordinates
import com.minskrotterdam.airquality.models.stations.ExtData
import com.minskrotterdam.airquality.services.StationInfoService
import com.minskrotterdam.airquality.services.StationsService
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.collections4.Trie
import org.apache.commons.collections4.trie.PatriciaTrie
import org.slf4j.LoggerFactory
import ru.gildor.coroutines.retrofit.await

class StationsHandler {
    val latLngRegex: Regex = Regex("^-?\\d+\\.\\d{2,}$")
    private val logger = LoggerFactory.getLogger("VertxServer")
    suspend fun stationsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getStations(ctx)
        }
    }

    suspend fun stationsLatitudeCoordinates(ctx: RoutingContext, stations: Trie<String, ExtData>) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getStations(ctx, stations)
        }
    }

    private fun validateNumber(num: String) {
        if (num.isEmpty() || !latLngRegex.matches(num)) {
            throw IllegalArgumentException("Constraint: 'lat/lng' query parameters must match the regex $latLngRegex")
        }
    }

    private fun validateLngLat(ctx: RoutingContext) {
        validateNumber(ctx.queryParam("lat")[0])
        validateNumber(ctx.queryParam("lng")[0])
    }

   suspend fun initStationsCache(): Trie<String, ExtData> {
        val job = Job()
       return CoroutineScope(Dispatchers.IO + job).async {
           val stationsCache = getStationsCache()
           stationsCache
       }.await()
   }

    private fun withFilter(ctx: RoutingContext): Boolean {
        return ctx.pathParam("location").toLowerCase() != "all"
    }

    private suspend fun getStations(ctx: RoutingContext, cache: Trie<String, ExtData>) {
        validateLngLat(ctx)
        val result = mutableListOf<ExtData>()
        try {
            val response = ctx.response()
            val latitude = ctx.queryParam("lat")[0]
            val longitude = ctx.queryParam("lng")[0]
            response.isChunked = true
            result.add(searchByLatLng(cache, latitude, longitude))
            response.write(Json.encode(result))
            response.endAwait()
        } finally {
            result.clear()
        }
    }

    private suspend fun getStations(ctx: RoutingContext) {
        val result: MutableList<ExtData> = mutableListOf()
        try {
            val response = ctx.response()
            val location = ctx.pathParam("location").toLowerCase()
            val withFilter = withFilter(ctx)
            response.isChunked = true
            val firstPage = StationsService().getStations(1).await()
            val firstStationsResult = if (withFilter) firstPage.data.filter { it.location.toLowerCase().contains(location) } else firstPage.data
            val extendedStationsFirstResult = mutableListOf<ExtData>()
            val mutex = Mutex()
            val safeLaunchRanges = getSafeLaunchRanges(firstStationsResult.size + 1)
            safeLaunchRanges.forEach { intRange ->
                intRange.map {
                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).async {
                        val stationData = firstStationsResult[it - 2]
                        val stationDetails = StationInfoService().getStationDetails(stationData.number).await()
                        val rawCoordinates = stationDetails.data.geometry.coordinates
                        val coordinates = Coordinates(rawCoordinates[0], rawCoordinates[1])

                        mutex.withLock {
                            val element = ExtData(stationData.number, stationData.location, stationDetails.data.municipality, coordinates, stationDetails.data.components)
                            extendedStationsFirstResult.add(element)
                        }
                    }
                }.awaitAll()
            }
            val pagination = firstPage.pagination
            mutex.withLock {
                if (firstStationsResult.isNotEmpty()) {
                    result.addAll(extendedStationsFirstResult)
                }
            }
            getSafeLaunchRanges(pagination.last_page).forEach { intRange ->
                intRange.map {
                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).async {
                        val stations = StationsService().getStations(it).await()
                        val locatedStations = if (withFilter) stations.data.filter { it.location.toLowerCase().contains(location) } else stations.data
                        val extendedStations = mutableListOf<ExtData>()
                        val safeLaunchRanges1 = getSafeLaunchRanges(locatedStations.size + 1)
                        safeLaunchRanges1.forEach { intRange ->
                            intRange.map {
                                val job1 = Job()
                                CoroutineScope(Dispatchers.IO + job1).async {
                                    val stationData = locatedStations[it - 2]
                                    val stationDetails = StationInfoService().getStationDetails(stationData.number).await()
                                    val rawCoordinates = stationDetails.data.geometry.coordinates
                                    val coordinates = Coordinates(rawCoordinates[0], rawCoordinates[1])
                                    mutex.withLock {
                                        val element = ExtData(stationData.number, stationData.location, stationDetails.data.municipality, coordinates, stationDetails.data.components)
                                        extendedStations.add(element)
                                    }
                                }
                            }.awaitAll()
                        }
                        mutex.withLock {
                            if (locatedStations.isNotEmpty()) {
                                result.addAll(extendedStations)
                            }
                        }
                    }
                }.awaitAll()
            }
            mutex.withLock {
                response.write(Json.encode(result))
                response.endAwait()
            }
        } finally {
            result.clear()
        }
    }

    private suspend fun getStationsCache(): Trie<String, ExtData> {
        val result = mutableListOf<ExtData>()
        val trie: Trie<String, ExtData> = PatriciaTrie()
        val firstPage = StationsService().getStations(1).await()
        val safeLaunchRanges = getSafeLaunchRanges(firstPage.data.size + 1)
        val m = Mutex()
        safeLaunchRanges.forEach { intRange ->
            intRange.map {
                val job = Job()
                CoroutineScope(Dispatchers.IO + job).async {
                    val stationData = firstPage.data[it - 2]
                    val stationDetails = StationInfoService().getStationDetails(stationData.number).await()
                    val rawCoordinates = stationDetails.data.geometry.coordinates
                    val coordinates = Coordinates(rawCoordinates[0], rawCoordinates[1])
                    val element = ExtData(stationData.number, stationData.location, stationDetails.data.municipality, coordinates, stationDetails.data.components)
                    m.withLock {
                        result.add(element)
                    }
                }
            }.awaitAll()
        }
        val pagination = firstPage.pagination
        getSafeLaunchRanges(pagination.last_page).forEach { intRange ->
            intRange.map {
                val job = Job()
                CoroutineScope(Dispatchers.IO + job).async {
                    val stations = StationsService().getStations(it).await()
                    val safeLaunchRanges1 = getSafeLaunchRanges(stations.data.size + 1)
                    safeLaunchRanges1.forEach { intRange ->
                        intRange.map {
                            val job1 = Job()
                            CoroutineScope(Dispatchers.IO + job1).async {
                                val stationData = stations.data[it - 2]
                                val stationDetails = StationInfoService().getStationDetails(stationData.number).await()
                                val rawCoordinates = stationDetails.data.geometry.coordinates
                                val coordinates = Coordinates(rawCoordinates[0], rawCoordinates[1])
                                val element = ExtData(stationData.number, stationData.location, stationDetails.data.municipality, coordinates, stationDetails.data.components)
                                m.withLock {
                                    result.add(element)
                                }
                            }
                        }.awaitAll()
                    }
                }
            }.awaitAll()
            m.withLock {
                result.forEach {
                    trie.put(it.coordinates.lat.toString(), it)
                }
            }
        }
        return trie
    }
}
