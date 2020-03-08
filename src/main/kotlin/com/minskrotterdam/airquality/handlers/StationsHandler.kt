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
        response.isChunked = true
        val firstPage = StationsService().getStations(1).await()
        val pagination = firstPage.pagination
        val mutex = Mutex()
        mutex.withLock {
            response.write("[")
            response.write(Json.encode(firstPage.data))
        }
        getSafeLaunchRanges(pagination.last_page).forEach {
            it.map {
                CoroutineScope(Dispatchers.Default).async {
                    val station = StationsService().getStations(it).await()
                    mutex.withLock {
                        response.write(",")
                        response.write(Json.encode(station.data))
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