package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.common.averageValueByComponent
import com.minskrotterdam.airquality.common.minMaxByComponent
import com.minskrotterdam.airquality.common.getSafeLaunchRanges
import com.minskrotterdam.airquality.common.safeLaunch
import com.minskrotterdam.airquality.metadata.RegionalStationsSegments
import com.minskrotterdam.airquality.services.MeasurementsService
import io.vertx.core.MultiMap
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.endAwait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.gildor.coroutines.retrofit.await
import java.time.Instant
import java.time.temporal.ChronoUnit

class AggregatedMeasurementsHandler {
    enum class Aggregate {
        MAX,
        MIN,
        AVG
    }

    private fun extractAggregatorParam(requestParameters: MultiMap): Aggregate {
        when (requestParameters.get("aggr")) {
            Aggregate.MAX.name.toLowerCase() -> return Aggregate.MAX
            Aggregate.MIN.name.toLowerCase() -> return Aggregate.MIN
            else -> return return Aggregate.AVG }
    }


    private fun extractAggregator(requestParameters: MultiMap): (Double, Double) -> Double {
        when (requestParameters.get("aggr")) {
            Aggregate.MAX.name.toLowerCase() -> return { a: Double, b: Double -> maxOf(a, b) }
            Aggregate.MIN.name.toLowerCase() -> return { a: Double, b: Double -> minOf(a, b) }
            else -> return { a: Double, b: Double -> maxOf(a, b)}
        }
    }

    private fun extractStationNumbers(ctx: RoutingContext): List<String>? {
        val regio = ctx.pathParam("region")
        if(regio.isNullOrEmpty()) {
            val stationId = ctx.pathParam("station_number")
            return if (stationId.isNotEmpty())
                listOf(stationId)
            else RegionalStationsSegments.segments["ZP"];
        }
        else {
            return RegionalStationsSegments.segments.getOrDefault(regio.toLowerCase(), RegionalStationsSegments.segments["zp"])
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
            return Instant.now().minus(6, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS).toString()
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
        val response = ctx.response()
        val requestParameters = ctx.request().params()
        val stationId = extractStationNumbers(ctx)

        val formula = extractFormula(requestParameters)
        val endTime = extractEndTime(requestParameters)
        val startTime = extractStartTime(requestParameters)
        response.isChunked = true
        val firstPage = MeasurementsService().getMeasurement(stationId!!, formula, startTime, endTime, 1).await()
        val pagination = firstPage.pagination
        val aggregatorFun = extractAggregator(requestParameters)
        val firstResult = if(extractAggregatorParam(requestParameters) == Aggregate.AVG) {
            firstPage.data.averageValueByComponent().toMutableList()
        } else {
            firstPage.data.minMaxByComponent(aggregatorFun).toMutableList()
        }

        val mutex = Mutex()
        //When too many coroutines are waiting for server response concurrently, the server may respond with 504 code
        //Use segmentation to smaller ranges as workaround
        getSafeLaunchRanges(pagination.last_page).forEach { it ->
            it.map {
                CoroutineScope(Dispatchers.Default).async {
                    val measurement = MeasurementsService().getMeasurement(stationId, formula, startTime,
                            endTime, it).await()
                    val message = if(extractAggregatorParam(requestParameters) == Aggregate.AVG) {
                        measurement.data.averageValueByComponent()
                    } else {
                        measurement.data.minMaxByComponent(aggregatorFun)
                    }
                    mutex.withLock {
                        firstResult.addAll(message)
                    }
                }
            }.awaitAll()
        }
        val result = firstResult.minMaxByComponent(aggregatorFun)
        mutex.withLock {
            response.write(Json.encode(result))
            response.endAwait()
        }
    }
}