package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.common.getSafeLaunchRanges
import com.minskrotterdam.airquality.common.safeLaunch
import com.minskrotterdam.airquality.models.measurements.Data
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

class StationMeasurementsHandler {

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
            return Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
        }
        return startTime
    }

    suspend fun airMeasurementsHandler(ctx: RoutingContext) {
        safeLaunch(ctx) {
            ctx.response().headers().set("Content-Type", "application/json")
            getAggregatedMeasurements(ctx)
        }
    }

    private suspend fun getAggregatedMeasurements(ctx: RoutingContext) {
        val response = ctx.response()
        val requestParameters = ctx.request().params()
        val stationId = ctx.pathParam("station_number")
        val formula = extractFormula(requestParameters)
        val endTime = extractEndTime(requestParameters)
        val startTime = extractStartTime(requestParameters)
        response.isChunked = true
        val firstPage = MeasurementsService().getMeasurement(listOf(stationId), formula, startTime, endTime, 1).await()
        val pagination = firstPage.pagination
        val message = firstPage.data.groupBy { it.formula }.toSortedMap().values.map { it ->
            it.reduce { ac, data ->
                Data(ac.formula,
                        ac.station_number,
                        ac.timestamp_measured, maxOf(ac.value, data.value))
            }
        }
        val mutex = Mutex()
        mutex.withLock {
            response.write("[")
            response.write(Json.encode(message))
        }
        //When too many coroutines are waiting for server response concurrently, the server may respond with 504 code
        //Use segmentation to smaller ranges as workaround
        getSafeLaunchRanges(pagination.last_page).forEach { it ->
            it.map {
                CoroutineScope(Dispatchers.Default).async {
                    val measurement = MeasurementsService().getMeasurement(listOf(stationId), formula, startTime,
                            endTime, it).await()
                    val message = measurement.data.groupBy { it.formula }.toSortedMap().values.map { it ->
                        it.reduce { it, data ->
                            Data(it.formula,
                                    it.station_number,
                                    it.timestamp_measured, maxOf(it.value, data.value))
                        }
                    }
                    mutex.withLock {
                        response.write(",")
                        response.write(Json.encode(message))
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