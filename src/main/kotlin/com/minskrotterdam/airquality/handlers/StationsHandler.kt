package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.getSafeLaunchRanges
import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.models.stations.Data
import com.minskrotterdam.airquality.services.StationsService
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.gildor.coroutines.retrofit.await

class StationsHandler {
    private val logger = LoggerFactory.getLogger("VertxServer")

    suspend fun stationsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getStations(ctx)
        }
    }

    private suspend fun getStations(ctx: RoutingContext) {
        val result: MutableList<Data> = mutableListOf()
        try {
            val response = ctx.response()
            val location = ctx.pathParam("location").toLowerCase()
            response.isChunked = true
            val firstPage = StationsService().getStations(1).await()
            val pagination = firstPage.pagination
            val mutex = Mutex()
            mutex.withLock {
                val stations = firstPage.data.filter { it.location.toLowerCase().contains(location) }
                if (stations.isNotEmpty()) {
                    result.addAll(stations)
                }
            }
            getSafeLaunchRanges(pagination.last_page).forEach { intRange ->
                intRange.map {
                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).async {
                        val stations = StationsService().getStations(it).await()
                        val locatedStations = stations.data.filter { it.location.toLowerCase().contains(location) }
                        mutex.withLock {
                            if (locatedStations.isNotEmpty()) {
                                result.addAll(locatedStations)
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

}