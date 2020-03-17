package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.common.getSafeLaunchRanges
import com.minskrotterdam.airquality.common.safeLaunch
import com.minskrotterdam.airquality.services.StationsService
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.gildor.coroutines.retrofit.await
import java.util.concurrent.atomic.AtomicBoolean

class StationsHandler {
    private val logger = LoggerFactory.getLogger("VertxServer")

    suspend fun stationsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getStations(ctx)
        }
    }

    private suspend fun getStations(ctx: RoutingContext) {
        val response = ctx.response()
        val location = ctx.pathParam("location").toLowerCase()
        response.isChunked = true
        val firstPage = StationsService().getStations(1).await()
        val pagination = firstPage.pagination
        val mutex = Mutex()
        val flag = AtomicBoolean(false)
        mutex.withLock {
            response.write("[")
            val stations = firstPage.data.filter { it.location.toLowerCase().contains(location) }
            if (stations.isNotEmpty()) {
                response.write(Json.encode(stations))
                flag.set(true)
            }
        }
        getSafeLaunchRanges(pagination.last_page).forEach {
            it.map {
                CoroutineScope(Dispatchers.Default).async {
                    val stations = StationsService().getStations(it).await()
                    val locatedStations = stations.data.filter { it.location.toLowerCase().contains(location) }
                    mutex.withLock {
                        if (locatedStations.isNotEmpty()) {
                            if (flag.get()) response.write(",")
                            response.write(Json.encode(locatedStations))

                        } else {
                            flag.set(false)
                        }
                    }
                }
            }.awaitAll()
        }
        mutex.withLock {
            response.write("]")
            response.endAwait()
        }
    }

}