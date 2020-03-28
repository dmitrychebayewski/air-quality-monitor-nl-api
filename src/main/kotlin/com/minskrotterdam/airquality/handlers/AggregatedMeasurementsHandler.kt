package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.config.HOURS_BACK
import com.minskrotterdam.airquality.extensions.getSafeLaunchRanges
import com.minskrotterdam.airquality.extensions.safeLaunch
import com.minskrotterdam.airquality.models.AggregateBy
import com.minskrotterdam.airquality.models.ext.aggregateBy
import com.minskrotterdam.airquality.models.measurements.Data
import com.minskrotterdam.airquality.models.metadata.STATIONS_SEGMENTED
import com.minskrotterdam.airquality.services.MeasurementsService
import io.vertx.core.MultiMap
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.gildor.coroutines.retrofit.await
import java.time.Instant
import java.time.temporal.ChronoUnit

class AggregatedMeasurementsHandler {

    private fun extractAggregatorParam(requestParameters: MultiMap): AggregateBy {
        return when (requestParameters.get("aggr")) {
            AggregateBy.MAX.name.toLowerCase() -> AggregateBy.MAX
            AggregateBy.MIN.name.toLowerCase() -> AggregateBy.MIN
            else -> return AggregateBy.AVG
        }
    }

    private fun extractStationNumbers(ctx: RoutingContext): List<String>? {
        val regio = ctx.pathParam("region")
        if (regio.isNullOrEmpty()) {
            val stationId = ctx.pathParam("station_number")
            return if (stationId.isNotEmpty())
                listOf(stationId)
            else STATIONS_SEGMENTED["zp"]
        } else {
            return STATIONS_SEGMENTED.getOrDefault(regio.toLowerCase(), STATIONS_SEGMENTED["zp"])
        }
    }

    private fun extractFormula(requestParameters: MultiMap?): String {
        val formula = requestParameters?.get("formula")
        if (formula.isNullOrEmpty()) {
            return ""
        }
        return formula
    }

    private fun extractEndTime(requestParameters: MultiMap): String {
        val endTime = requestParameters.get("end")
        if (endTime.isNullOrEmpty()) {
            return Instant.now().toString()
        }
        return endTime
    }

    private fun extractStartTime(requestParameters: MultiMap): String {
        val startTime = requestParameters.get("start")
        if (startTime.isNullOrEmpty()) {
            return Instant.now().minus(HOURS_BACK, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toString()
        }
        return startTime
    }

    suspend fun airMeasurementsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedMeasurements(ctx)
        }
    }

    suspend fun aggregatedAirMeasurementsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedMeasurements(ctx)
        }
    }

    private suspend fun getAggregatedMeasurements(ctx: RoutingContext) {
        var firstResult = mutableListOf<Data>()
        try {
            val response = ctx.response()
            val requestParameters = ctx.request().params()
            val stationId = extractStationNumbers(ctx)

            val formula = extractFormula(requestParameters)
            val endTime = extractEndTime(requestParameters)
            val startTime = extractStartTime(requestParameters)
            response.isChunked = true
            val firstPage = MeasurementsService().getMeasurement(stationId!!, formula, startTime, endTime, 1).await()
            val pagination = firstPage.pagination
            val by = extractAggregatorParam(requestParameters)
            firstResult = firstPage.data.aggregateBy(extractAggregatorParam(requestParameters))
            val mutex = Mutex()
            //When too many coroutines are waiting for server response concurrently, the server may respond with 504 code
            //Use segmentation to smaller ranges as workaround
            getSafeLaunchRanges(pagination.last_page).forEach { it ->
                it.map {
                    val job = Job()
                    CoroutineScope(Dispatchers.Default + job).async {
                        val measurement = MeasurementsService().getMeasurement(stationId, formula, startTime,
                                endTime, it).await()
                        mutex.withLock {
                            firstResult.addAll(measurement.data.aggregateBy(by))
                        }
                    }
                }.awaitAll()
            }
            mutex.withLock {
                response.write(Json.encode(firstResult.aggregateBy(by)))
                response.endAwait()
            }

        } finally {
            firstResult.clear()
        }
    }
}