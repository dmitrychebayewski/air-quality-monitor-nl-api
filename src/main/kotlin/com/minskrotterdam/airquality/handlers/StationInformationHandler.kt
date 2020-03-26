package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.services.StationInfoService
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.gildor.coroutines.retrofit.await

class StationInformationHandler {

    suspend fun stationInformationHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getStationDetails(ctx)
        }
    }

    private suspend fun getStationDetails(ctx: RoutingContext) {
        val response = ctx.response()
        response.isChunked = true
        val stationNr = ctx.pathParam("station_number")
        val stationInfo = StationInfoService().getStationDetails(stationNr).await()
        val mutex = Mutex()
        mutex.withLock {
            response.write(Json.encode(stationInfo))
            response.endAwait()
        }
    }
}
