package com.minskrotterdam.airquality.handlers

import com.minskrotterdam.airquality.config.HOURS_BACK
import com.minskrotterdam.airquality.extensions.getSafeLaunchRanges
import com.minskrotterdam.airquality.models.AggregateBy
import com.minskrotterdam.airquality.models.ext.aggregateBy
import com.minskrotterdam.airquality.models.measurements.Data
import com.minskrotterdam.airquality.models.metadata.STATIONS_SEGMENTED
import com.minskrotterdam.airquality.services.MeasurementsService
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.gildor.coroutines.retrofit.await
import java.time.Instant
import java.time.temporal.ChronoUnit

abstract class BaseAggregatedMeasurementsHandler(val vertx: Vertx) {

    private fun extractAggregatorParam(requestParameters: MultiMap): AggregateBy {
        return when (requestParameters.get("aggr")) {
            AggregateBy.MAX.name.toLowerCase() -> AggregateBy.MAX
            AggregateBy.MIN.name.toLowerCase() -> AggregateBy.MIN
            else -> return AggregateBy.AVG
        }
    }

    private fun extractStationNumbers(regio: String?, stationId: String?): List<String>? {
       if (regio.isNullOrEmpty()) {
            return if (!stationId.isNullOrEmpty())
                listOf(stationId)
            else STATIONS_SEGMENTED["zp"]
        } else {
            return STATIONS_SEGMENTED.getOrDefault(regio.toLowerCase(), STATIONS_SEGMENTED["zp"])
        }
    }

    protected fun extractFormula(requestParameters: MultiMap?): String {
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

    protected suspend fun get(requestParameters: MultiMap, regio: String?, stationId: String?): String {
        var firstResult = mutableListOf<Data>()
        try {

            val formula = extractFormula(requestParameters)
            val endTime = extractEndTime(requestParameters)
            val startTime = extractStartTime(requestParameters)
            val stationIdList = extractStationNumbers(regio, stationId)
            val firstPage = MeasurementsService().getMeasurement(stationIdList!!, formula, startTime, endTime, 1).await()
            val pagination = firstPage.pagination
            val by = extractAggregatorParam(requestParameters)
            firstResult = firstPage.data.aggregateBy(extractAggregatorParam(requestParameters))
            val mutex = Mutex()
            //When too many coroutines are waiting for server response concurrently, the server may respond with 504 code
            //Use segmentation to smaller ranges as workaround
            getSafeLaunchRanges(pagination.last_page).forEach { it ->
                it.map {
                    val job = Job()
                    CoroutineScope(Dispatchers.IO + job).async {
                        val measurement = MeasurementsService().getMeasurement(stationIdList, formula, startTime,
                                endTime, it).await()
                        mutex.withLock(firstResult) {
                            firstResult.addAll(measurement.data.aggregateBy(by))
                        }
                    }
                }.awaitAll()
            }
            return Json.encode(firstResult.aggregateBy(by))
        } finally {
            firstResult.clear()
        }
    }

    protected fun regio(regio: String)  = vertx.sharedData().getLocalMap<String, String>(regio)
}